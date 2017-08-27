package jw.zookeeper.utils;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
/**
 * @author yinjiawei
 */
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class JwZooKeeper implements Watcher {

	protected CountDownLatch countDownLatch = new CountDownLatch(1);
	// 缓存时间
	private static final int SESSION_TIME = 2000;
	public static ZooKeeper zooKeeper = null;

	/**
	 * 监控所有被触发的事件
	 */  
    public void process(WatchedEvent event) {  
    	
        if(event.getState()==KeeperState.SyncConnected){  
        	System.out.println("core node connected");
            countDownLatch.countDown();    
        }   
        if(event.getType()==EventType.NodeDeleted){    
            System.out.println("NodeDeleted");    
        } 
        if(event.getType()==EventType.NodeDataChanged){    
            System.out.println("NodeDataChanged");    
        } 
        if(event.getType()==EventType.NodeCreated){
        	System.out.println("NodeCreated"); 
        }
        if(event.getType()==EventType.NodeChildrenChanged){
        	System.out.println("NodeChildrenChanged");
        }
    }  
      
   
    public void connect(String hosts){       
        try {  
            if(zooKeeper == null){  
                zooKeeper = new ZooKeeper(hosts,SESSION_TIME,this);     
                countDownLatch.await();  
            }  
        } catch (IOException e) {  
        e.printStackTrace();
        } catch (InterruptedException e) {  
          e.printStackTrace();
        }       
    }     
      
    
    public void close(){       
        try {  
            if(zooKeeper != null){  
                zooKeeper.close();  
            }  
        } catch (InterruptedException e) {  
            e.printStackTrace();
        }       
    }    
}  