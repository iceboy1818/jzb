package io.zbus.mq;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.zbus.mq.Protocol.ServerAddress;
import io.zbus.mq.Protocol.ServerInfo;
import io.zbus.mq.Protocol.TrackerInfo;
import io.zbus.mq.kit.JsonKit;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import io.zbus.mq.net.EventLoop;
import io.zbus.mq.net.MessageClient.ConnectedHandler;
import io.zbus.mq.net.MessageClient.DisconnectedHandler;
import io.zbus.mq.net.MessageClient.MsgHandler;

public class Broker implements Closeable { 
	private static final Logger log = LoggerFactory.getLogger(Broker.class); 
	
	private Map<ServerAddress, MqClientPool> poolTable = new ConcurrentHashMap<ServerAddress, MqClientPool>();   
	private BrokerRouteTable routeTable = new BrokerRouteTable(); 
	
	private Map<ServerAddress, MqClient> trackerSubscribers = new ConcurrentHashMap<ServerAddress, MqClient>(); 
	
	
	
	private List<ServerNotifyListener> listeners = new ArrayList<ServerNotifyListener>(); 
	private EventLoop eventLoop;  
	private final int clientPoolSize; 
	private Map<String, String> sslCertFileTable;
	private String defaultSslCertFile;
	
	private CountDownLatch ready = new CountDownLatch(1);
	private boolean waitCheck = true;
	
	public Broker(){
		this(new BrokerConfig());
	} 
	
	public Broker(BrokerConfig config){
		this.eventLoop = new EventLoop(); 
		this.clientPoolSize = config.getClientPoolSize();  
		this.sslCertFileTable = config.getSslCertFileTable();
		this.defaultSslCertFile = config.getDefaultSslCertFile(); 
		
		List<ServerAddress> trackerList = config.getTrackerList(); 
		for(ServerAddress serverAddress : trackerList){ 
			addTracker(serverAddress); 
		}   
	}   
	
	public Broker(String trackerList){
		this.eventLoop = new EventLoop(); 
		this.clientPoolSize = 32;
		
		String[] bb = trackerList.split("[,; ]");
		for(String tracker : bb){
			tracker = tracker.trim();
			if(tracker.isEmpty()) continue; 
			addTracker(tracker);
		}
	} 
 
	public void addTracker(String trackerAddress){
		addTracker(trackerAddress, null);
	} 
	
	public void addTracker(final ServerAddress trackerAddress){
		addTracker(trackerAddress, null);
	}
	
	public void addTracker(String trackerAddress, String certPath){
		ServerAddress serverAddress = new ServerAddress(trackerAddress);
		serverAddress.sslEnabled = certPath != null; 
		
		addTracker(serverAddress, certPath);
	} 
	
	public void addTracker(final ServerAddress trackerAddress, String certPath){
		if(trackerSubscribers.containsKey(trackerAddress)) return; 
		
		if(certPath != null){
			sslCertFileTable.put(trackerAddress.address, certPath);
		} 
		final AtomicReference<ServerAddress> remoteTrackerAddress = new AtomicReference<ServerAddress>(trackerAddress);
		final MqClient client = connectToServer(trackerAddress); 
		client.onDisconnected(new DisconnectedHandler() { 
			
			public void onDisconnected() throws IOException { 
				ServerAddress trackerAddress = remoteTrackerAddress.get();
				log.warn("Disconnected from tracker(%s)", trackerAddress);  
				List<ServerAddress> toRemove = routeTable.removeTracker(trackerAddress);  
				if(!toRemove.isEmpty()){ 
					for(ServerAddress serverAddress : toRemove){
						removeServer(serverAddress);
					}
				}  
				client.ensureConnectedAsync(); 
			}  
		});
		
		client.onConnected(new ConnectedHandler() {
			
			public void onConnected() throws IOException { 
				ServerAddress trackerAddress = remoteTrackerAddress.get();
				log.info("Connected to tracker(%s)", trackerAddress);  
				
				Message req = new Message();  
				req.setCommand(Protocol.TRACK_SUB);
				req.setAck(false); 
				client.sendMessage(req);
			}
		}); 
		
		client.onMessage(new MsgHandler() {  
			
			public void handle(Message msg) throws IOException { 
				if(Protocol.TRACK_PUB.equals(msg.getCommand())){  
					TrackerInfo trackerInfo = JsonKit.parseObject(msg.getBodyString(), TrackerInfo.class);
					
					remoteTrackerAddress.set(trackerInfo.serverAddress);
					//remote tracker's real address obtained, update ssl cert file mapping
					if(trackerInfo.serverAddress.sslEnabled){
						String sslCertFile = sslCertFileTable.get(trackerAddress.address);
						if(sslCertFile != null){
							sslCertFileTable.put(trackerInfo.serverAddress.address, sslCertFile);
						}
					} 
					
					List<ServerAddress> toRemove = routeTable.updateTracker(trackerInfo);
					for(ServerInfo serverInfo : routeTable.serverTable().values()){
						addServer(serverInfo);
					}
					
					if(!toRemove.isEmpty()){ 
						for(ServerAddress serverAddress : toRemove){
							removeServer(serverAddress);
						}
					}  
					
					if(waitCheck){
						ready.countDown();
					}
				}
			}
		});
		
		client.ensureConnectedAsync();
		trackerSubscribers.put(trackerAddress, client); 
	} 
	
	private void addServer(final ServerInfo serverInfo) throws IOException {   
		MqClientPool pool = null;  
		final ServerAddress serverAddress = serverInfo.serverAddress;
		synchronized (poolTable) {
			pool = poolTable.get(serverAddress);
			if(pool != null) return; 
			 
			try{
				pool = createMqClientPool(serverAddress, serverAddress);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			} 
			poolTable.put(serverAddress, pool);  
		}   
		
		final MqClientPool createdPool = pool;
		eventLoop.getGroup().submit(new Runnable() { 
			
			public void run() {  
				try { 
					for(final ServerNotifyListener listener : listeners){
						eventLoop.getGroup().submit(new Runnable() { 
							
							public void run() {  
								listener.onServerJoin(createdPool);
							}
						});
					} 
				} catch (Exception e) { 
					log.error(e.getMessage(), e);
				}  
			}
		}); 
	}  
	 
	private void removeServer(final ServerAddress serverAddress) { 
		final MqClientPool pool;
		synchronized (poolTable) { 
			pool = poolTable.remove(serverAddress);
			if(pool == null) return;   
		}    
		
		eventLoop.getGroup().schedule(new Runnable() { 
			
			public void run() {
				try {
					pool.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				} 
			}
		}, 1000, TimeUnit.MILLISECONDS); //delay 1s to close to wait other service depended on this broker
		
		for(final ServerNotifyListener listener : listeners){
			eventLoop.getGroup().submit(new Runnable() { 
				
				public void run() { 
					listener.onServerLeave(serverAddress);
				}
			});
		}
	}  
	
	
	public void close() throws IOException {
		for(MqClient client : trackerSubscribers.values()){
			client.close();
		}
		trackerSubscribers.clear();
		synchronized (poolTable) {
			for(MqClientPool pool : poolTable.values()){ 
				pool.close();
			}
			poolTable.clear();
		}  
		
		eventLoop.close();
	}   
	
	public MqClientPool[] selectClient(ServerSelector selector, Message msg) {
		checkReady();
		
		ServerAddress[] serverList = selector.select(routeTable, msg);
		if(serverList == null){
			return poolTable.values().toArray(new MqClientPool[0]);
		}
		
		MqClientPool[] pools = new MqClientPool[serverList.length];
		int count = 0;
		for(int i=0; i<serverList.length; i++){
			pools[i] = poolTable.get(serverList[i]);
			if(pools[i] != null) count++;
		} 
		if(count == serverList.length) return pools; 

		MqClientPool[] pools2 = new MqClientPool[count];
		int j = 0;
		for(int i=0; i<pools.length; i++){
			if(pools[i] != null){
				pools2[j++] = pools[i];
			}
		} 
		return pools2;
	} 
	
	
	public void addSslCertFile(String address, String certPath){
		sslCertFileTable.put(address, certPath);
	}
	
	
	public void addServerNotifyListener(ServerNotifyListener listener) {
		this.listeners.add(listener);
	}
 
	public void removeServerNotifyListener(ServerNotifyListener listener) {
		this.listeners.remove(listener);
	} 
	 
	 
	private void checkReady(){
		if(waitCheck){
			try {
				ready.await(3000, TimeUnit.MILLISECONDS); 
			} catch (InterruptedException e) {
				//ignore 
			}
			waitCheck = false;
		}
	}

	private MqClient connectToServer(ServerAddress serverAddress){
		EventLoop driver = eventLoop.duplicate(); //duplicated, no need to close
		if(serverAddress.sslEnabled){
			String certPath = sslCertFileTable.get(serverAddress.address);
			if(certPath == null) certPath = defaultSslCertFile;
			if(certPath == null){
				throw new IllegalStateException(serverAddress + " certificate file not found");
			}
			driver.setClientSslContext(certPath); 
		}
		
		final MqClient client = new MqClient(serverAddress.address, driver);  
		return client;
	}
	
	private MqClientPool createMqClientPool(ServerAddress remoteServerAddress, ServerAddress serverAddress){
		EventLoop driver = eventLoop.duplicate(); //duplicated, no need to close
		if(serverAddress.sslEnabled){
			String certPath = sslCertFileTable.get(remoteServerAddress.address);
			if(certPath == null) certPath = sslCertFileTable.get(serverAddress.address);
			if(certPath == null) certPath = defaultSslCertFile;
			
			if(certPath == null){
				throw new IllegalStateException(serverAddress + " certificate file not found");
			}
			driver.setClientSslContext(certPath);
		}
		return new MqClientPool(serverAddress.address, clientPoolSize, driver);   
	} 
	
	public void setDefaultSslCertFile(String defaultSslCertFile) {
		this.defaultSslCertFile = defaultSslCertFile;
	} 
	
	public static interface ServerSelector { 
		ServerAddress[] select(BrokerRouteTable table, Message message); 
	} 
	
	public static interface ServerNotifyListener { 
		void onServerJoin(MqClientPool server); 
		void onServerLeave(ServerAddress serverAddress);
	}	
}
