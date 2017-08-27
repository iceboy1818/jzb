package io.zbus.mq;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.zbus.mq.MqClientPool.Pool.ObjectFactory;
import io.zbus.mq.Protocol.ServerAddress;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import io.zbus.mq.net.EventLoop; 

public class MqClientPool implements Closeable {
	private Pool<MqClient> pool;
	private MqClientFactory factory;

	private ServerAddress serverAddress;
	private final int clientPoolSize;
	private EventLoop eventDriver;

	public MqClientPool(String serverAddress, int clientPoolSize, EventLoop eventDriver) {
		this.clientPoolSize = clientPoolSize;
		if (eventDriver != null) {
			this.eventDriver = eventDriver.duplicate();
		} else {
			this.eventDriver = new EventLoop();
		}
		this.serverAddress = new ServerAddress(serverAddress, this.eventDriver.isSslEnabled());

		this.factory = new MqClientFactory(serverAddress, eventDriver);
		this.pool = new Pool<MqClient>(factory, this.clientPoolSize);

	}

	public MqClientPool(String serverAddress, int clientPoolSize) {
		this(serverAddress, clientPoolSize, null);
	}

	public MqClientPool(String serverAddress) {
		this(serverAddress, 64);
	}

	public MqClient borrowClient() {
		try {
			MqClient client = this.pool.borrowObject();
			return client;
		} catch (Exception e) {
			throw new MqException(e.getMessage(), e);
		}
	}

	public void returnClient(MqClient... client) {
		if (client == null || client.length == 0)
			return;
		for (MqClient c : client) {
			this.pool.returnObject(c);
		}
	}

	public MqClient createClient() {
		return factory.createObject();
	}

	public ServerAddress serverAddress() {
		return serverAddress;
	}

	
	public void close() throws IOException {
		if (this.pool != null) {
			this.pool.close();
			eventDriver.close();
			this.pool = null;
		}
	}

	public static class MqClientFactory implements ObjectFactory<MqClient>, Closeable {
		private static final Logger log = LoggerFactory.getLogger(MqClientFactory.class);

		protected final String serverAddress;
		protected EventLoop eventLoop;
		protected boolean ownEventDriver = false;

		public MqClientFactory(String serverAddress, EventLoop eventLoop) {
			this.serverAddress = serverAddress;
			this.eventLoop = eventLoop;
		}

		public String getServerAddress() {
			return serverAddress;
		}

		public MqClient createObject() {
			return new MqClient(serverAddress, eventLoop);
		}

		
		public boolean validateObject(MqClient client) {
			if (client == null)
				return false;
			return client.hasConnected();
		}

		
		public void destroyObject(MqClient client) {
			try {
				client.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}

		
		public void close() throws IOException {
			if (ownEventDriver && eventLoop != null) {
				eventLoop.close();
				eventLoop = null;
			}
		}
	}

	public static class Pool<T> implements Closeable {
		public static interface ObjectFactory<T> {

			T createObject() throws Exception;

			void destroyObject(T obj);

			boolean validateObject(T obj);
		}

		private ObjectFactory<T> factory;
		private BlockingQueue<T> queue = null;
		private final int maxTotal;
		private final AtomicInteger activeCount = new AtomicInteger(0);

		public Pool(ObjectFactory<T> factory, int maxTotal) {
			this.factory = factory;

			this.maxTotal = maxTotal;
			this.queue = new ArrayBlockingQueue<T>(maxTotal);
		}

		
		public void close() throws IOException {
			T obj = null;
			while ((obj = queue.poll()) != null) {
				factory.destroyObject(obj);
			}
		}

		public T borrowObject() throws Exception {
			T obj = null;
			if (activeCount.get() >= maxTotal) {
				obj = queue.take();
				return obj;
			}
			obj = queue.poll();
			if (obj != null)
				return obj;

			obj = factory.createObject();
			activeCount.incrementAndGet();

			return obj;
		}

		public void returnObject(T obj) {
			if (!factory.validateObject(obj)) {
				activeCount.decrementAndGet();
				factory.destroyObject(obj);
				return;
			}
			queue.offer(obj);
		}
	}
}
