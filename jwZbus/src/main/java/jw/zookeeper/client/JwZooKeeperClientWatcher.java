package jw.zookeeper.client;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import jw.zookeeper.utils.JwZooKeeperDataContainer;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class JwZooKeeperClientWatcher implements Watcher {
	

	private List<String >serviceList;
	
	private JwZooKeeperOperate jwZooKeeperOperate=new JwZooKeeperOperate();
	
	 public void process(WatchedEvent event) {
         // TODO Auto-generated method stub
		
         if (event.getType() == EventType.NodeDataChanged) {  
             System.out.println("custom NodeDataChanged"); 
            // serviceList=jwZooKeeperOperate.getChild("/jw");
         }  
         if (event.getType() == EventType.NodeDeleted){  
             System.out.println("custom NodeDeleted");  
         }  
         if(event.getType()== EventType.NodeCreated){ 
             System.out.println("custom NodeCreated"); 
         }
         if(event.getType()==EventType.NodeChildrenChanged){
        	 System.out.println("custom NodeChildrenChanged");
        	 serviceList= jwZooKeeperOperate.getChildren("/jwZbus",new JwZooKeeperClientWatcher());
        	 JwZooKeeperDataContainer.setServiceList(serviceList);
         }
	 }
}
