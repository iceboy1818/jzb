package jw.zookeeper.producer;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import jw.zookeeper.client.JwZooKeeperClientWatcher;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class JwZooKeeperProducerWatcher implements Watcher {

	private List<String> serviceList;

	private JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();

	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		if (event.getType() == EventType.NodeDataChanged) {
			System.out.println("change");
			serviceList = jwZooKeeperOperate.getChildren("",new JwZooKeeperClientWatcher());
		}
		if (event.getType() == EventType.NodeDeleted) {
			System.out.println("dele");
		}
		if (event.getType() == EventType.NodeCreated) {
			System.out.println("create");
		}
	}
}
