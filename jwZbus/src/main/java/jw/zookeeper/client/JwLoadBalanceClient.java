package jw.zookeeper.client;

import java.util.List;
import java.util.Random;

import jw.zookeeper.utils.JwZooKeeper;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class JwLoadBalanceClient {
	 public static void main(String[] args) { 
	    	
	    	
	    	
	    	JwLoadBalanceClient JwLoadBalanceConsumer=new JwLoadBalanceClient();
	    	JwLoadBalanceConsumer.con();
	    	}
	public void con() {
		final JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();
		JwZooKeeper zooKeeper = new JwZooKeeper();
		zooKeeper.connect("192.168.99.100:2181");
		String serviceName = "/service"+Math.random();
		final String servicePath = "/jw" ;// 服务节点路径
		final List<String> serviceList;
//		boolean serviceExists = jwZooKeeperOperate.isExists(servicePath, new JwZooKeeperConsumerWatcher());
//		if (serviceExists) {// 服务存在，取服务地址
			serviceList = jwZooKeeperOperate.getChildren("/jw",new JwZooKeeperClientWatcher());
			for(String s:serviceList){
				System.out.println(s);
			}
			jwZooKeeperOperate.createZNode("/jw"+serviceName,"127.0.0.1");
//			//
//			//jwZooKeeperOperate.createZNode("/jw"+serviceName, "1");
//			jwZooKeeperOperate.deteleZNode(servicePath);
//			//System.out.println("1");
//		} else {
//			jwZooKeeperOperate.createZNode(servicePath, "1");
//		}
//		else{
//			jwZooKeeperOperate.createZNode(servicePath, "service");
//		}
		
	}
}
