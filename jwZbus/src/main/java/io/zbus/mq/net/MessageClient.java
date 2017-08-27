package io.zbus.mq.net;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslContext;
import io.zbus.mq.Message;
import io.zbus.mq.MessageCallback;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import io.zbus.mq.net.Sync.Ticket;

public class MessageClient implements Closeable { 
	private static final Logger log = LoggerFactory.getLogger(MessageClient.class); 
	protected final String host;
	protected final int port;
	
	protected int invokeTimeout = 3000;
	protected int connectTimeout = 3000;
	protected int heartbeatInterval = 60000; // 60 seconds
	
	
	protected Bootstrap bootstrap;
	protected final EventLoopGroup group;
	protected SslContext sslCtx;
	protected ChannelFuture channelFuture;
	protected Session session;  

	protected final Sync sync = new Sync(); 
	private int packageSizeLimit = 1024 * 1024 * 32; // maximum of 32M
	private CountDownLatch connectedEvent = new CountDownLatch(1);

	public MessageClient(String address, EventLoop eventLoop) {
		group = eventLoop.getGroup();
		if (this.group == null) {
			throw new IllegalArgumentException("group missing");
		}
		
		sslCtx = eventLoop.getSslContext();
		this.packageSizeLimit = eventLoop.getPackageSizeLimit();

		String[] bb = address.split(":");
		if (bb.length > 2) {
			throw new IllegalArgumentException("Address invalid: " + address);
		}
		host = bb[0].trim();
		if (bb.length > 1) {
			port = Integer.valueOf(bb[1]);
		} else {
			port = 80;
		}

		final String serverAddress = String.format("%s%s:%d", sslCtx == null ? "" : "[SSL]", host, port);
		onConnected(new ConnectedHandler() {
			
			public void onConnected() throws IOException {
				String msg = String.format("Connection(%s) OK", serverAddress);
				log.info(msg);
			}
		});

		onDisconnected(new DisconnectedHandler() {
			
			public void onDisconnected() throws IOException {
				log.warn("Disconnected from(%s)", serverAddress);
				ensureConnectedAsync();// automatically reconnect by default
			}
		});

		startHeartbeat();
	} 
	
	private void init() {
		if (bootstrap != null) return; 
		
		bootstrap = new Bootstrap();
		
		bootstrap.group(this.group).channel(NioSocketChannel.class).handler(
				new ChannelInitializer<SocketChannel>() { 
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				if (sslCtx != null) {
					p.addLast(sslCtx.newHandler(ch.alloc()));
				}

				p.addLast(new HttpRequestEncoder());
				p.addLast(new HttpResponseDecoder());
				p.addLast(new HttpObjectAggregator(packageSizeLimit));
				p.addLast(new MessageCodec());
				
				session = new NettySession();
				p.addLast((NettySession)session);
			}
		});
	}   

	public boolean hasConnected() {
		return session != null && session.isActive();
	}  
	
	private Object connectLock = new Object();
	public void connect() throws IOException, InterruptedException {
		if (hasConnected()) return;

		synchronized (connectLock) {
			if (!hasConnected()) {
				init();  
				channelFuture = bootstrap.connect(host, port); 
				connectedEvent.await(connectTimeout, TimeUnit.MILLISECONDS);
				channelFuture.sync();
				
				if (hasConnected())  return; 
				
				String msg = String.format("Connection(%s:%d) timeout", host, port);
				log.warn(msg);
				closeSession();  
				
				throw new IOException(msg);
			}
		}
	} 
	
	private void closeSession() throws IOException {
		if (session != null) {
			session.close();
			session = null; 
			connectedEvent = new CountDownLatch(1);
		}
	} 

	public void sendMessage(Message req) throws IOException {
		if (!hasConnected()) {
			try {
				connect();
			} catch (InterruptedException e) { 
				log.warn(e.getMessage(), e);
				return;
			} 
		}
		String id = req.getId();
		if (id == null) { // if message id missing, set it
			req.setId(Sync.nextId()); 
		} 
		session.write(req);
	}
	
	public void invokeAsync(Message req, MessageCallback callback) throws IOException {
		Ticket ticket = null;
		if (callback != null) {
			ticket = sync.createTicket(req, invokeTimeout, callback);
		}  
		try {
			sendMessage(req);
		} catch (IOException e) {
			if (ticket != null) {
				sync.removeTicket(ticket.getId());
			}
			throw e;
		}  
	} 
	
	public Message invokeSync(Message req, int timeout) throws IOException, InterruptedException {
		Ticket ticket = null;
		try {
			ticket = sync.createTicket(req, timeout);
			sendMessage(req);
			if (!ticket.await(timeout, TimeUnit.MILLISECONDS)) {
				return null;
			}
			return ticket.response();
		} finally {
			if (ticket != null) {
				sync.removeTicket(ticket.getId());
			}
		}
	}
	

	
	public void close() throws IOException {
		onConnected(null);
		onDisconnected(null);

		if (asyncConnectThread != null) {
			asyncConnectThread.interrupt();
			asyncConnectThread = null;
		}

		closeSession();
		
		if (heartbeator != null) {
			heartbeator.shutdownNow();
			heartbeator = null;
		}
	}
	
	private Thread asyncConnectThread; 
	public void ensureConnectedAsync() {
		if (hasConnected()) return;
		if (asyncConnectThread != null) return; 
		asyncConnectThread = new Thread(new Runnable() {
			
			public void run() {
				try {
					while (!hasConnected()) {
						try {
							connect();
						} catch (IOException e) {
							String msg = String.format("Trying again in %.1f seconds", connectTimeout / 1000.0);
							log.warn(msg);
							Thread.sleep(connectTimeout);
						} catch (InterruptedException e) {
							throw e;
						} catch (Exception e) {
							log.error(e.getMessage(), e);
							break;
						}
					}
					asyncConnectThread = null;
				} catch (InterruptedException e) {
					// ignore
				}  
			}
		});
		asyncConnectThread.setName("ClientConnectionAync");
		asyncConnectThread.start();
	}
	
	protected volatile ScheduledExecutorService heartbeator = null; 
	protected synchronized void startHeartbeat() {
		if (heartbeator == null) {
			heartbeator = Executors.newSingleThreadScheduledExecutor();
			this.heartbeator.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						heartbeat();
					} catch (Exception e) {
						log.warn(e.getMessage(), e);
					}
				}
			}, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
		}
	}

	protected void heartbeat() {
		if (this.hasConnected()) {
			Message hbt = new Message();
			hbt.setCommand(Message.HEARTBEAT);
			try {
				this.invokeAsync(hbt, (MessageCallback) null);
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private ConcurrentMap<String, Object> attributes = null;
	@SuppressWarnings("unchecked")
	public <V> V attr(String key) {
		if (this.attributes == null) {
			return null;
		}

		return (V) this.attributes.get(key);
	}

	public <V> void attr(String key, V value) {
		if (value == null) {
			if (this.attributes != null) {
				this.attributes.remove(key);
			}
			return;
		}
		if (this.attributes == null) {
			synchronized (this) {
				if (this.attributes == null) {
					this.attributes = new ConcurrentHashMap<String, Object>();
				}
			}
		}
		this.attributes.put(key, value);
	} 
	
 
	private class NettySession extends ChannelInboundHandlerAdapter implements Session {
		private ChannelHandlerContext ctx;

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			onMessage(msg);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			onError(cause);
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			this.ctx = ctx; 
			onActive();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			onInactive();
		}

		public void write(Object msg) {
			if (ctx == null) {
				throw new IllegalStateException("Context missing, lost connection?");
			}
			ctx.writeAndFlush(msg);
		} 

		
		public void close() throws IOException {
			if(ctx != null) {
				ctx.close(); 
			}
			
			sync.clearTicket();

			if (disconnectedHandler != null) {
				disconnectedHandler.onDisconnected();
			}
		}

		public boolean isActive() {
			return ctx != null && ctx.channel().isActive();
		}

		
		public void onMessage(Object message) throws IOException {
			Message res = (Message) message;
			Ticket ticket = sync.removeTicket(res);
			if (ticket != null) {
				ticket.notifyResponse(res);
				return;
			}

			if (msgHandler != null) {
				msgHandler.handle(res);
				return;
			}

			log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!Drop,%s", res);
		}

		
		public void onError(Throwable error) throws Exception  { 
			if (errorHandler != null) {
				errorHandler.onError(error);
			} else {
				log.error(error.getMessage(), error);
			}
		}

		
		public void onActive() throws IOException{
			connectedEvent.countDown();
			if (connectedHandler != null) {
				connectedHandler.onConnected();
			}
		}

		
		public void onInactive() throws IOException{
			close();
		}  
	}
	
	public static interface ConnectedHandler {
		void onConnected() throws IOException;
	}

	public static interface DisconnectedHandler {
		void onDisconnected() throws IOException;
	}

	public static interface ErrorHandler {
		void onError(Throwable e) throws IOException;
	}

	public static interface MsgHandler {
		void handle(Message msg) throws IOException;
	}
	
	protected volatile MsgHandler msgHandler;
	protected volatile ErrorHandler errorHandler;
	protected volatile ConnectedHandler connectedHandler;
	protected volatile DisconnectedHandler disconnectedHandler;
	
	public void onMessage(MsgHandler msgHandler) {
		this.msgHandler = msgHandler;
	}

	public void onError(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void onConnected(ConnectedHandler connectedHandler) {
		this.connectedHandler = connectedHandler;
	}

	public void onDisconnected(DisconnectedHandler disconnectedHandler) {
		this.disconnectedHandler = disconnectedHandler;
	}  

	public int getInvokeTimeout() {
		return invokeTimeout;
	}

	public void setInvokeTimeout(int invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}
}
