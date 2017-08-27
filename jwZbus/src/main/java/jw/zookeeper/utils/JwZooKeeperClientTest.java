package jw.zookeeper.utils;

import java.util.List;

import Test.SockerClient;
import jw.load.balance.utils.RoundRobinBalance;
import jw.zookeeper.client.JwZooKeeperClientWatcher;

public class JwZooKeeperClientTest {

	public static void main(String[] args) throws Exception {
		
		
		JwZooKeeper jwZooKeeper = new JwZooKeeper();
		jwZooKeeper.connect("192.168.99.100:2181,192.168.99.100:2182,192.168.99.100:2183");
		String serviceName = "/jwZbus";
		List<String> serviceList;
		JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();
		
		while (true) {
		Boolean serviceNameExistsFlag = jwZooKeeperOperate.isExists(serviceName, new JwZooKeeperClientWatcher());
		
		if (serviceNameExistsFlag) {
			serviceList = jwZooKeeperOperate.getChildren(serviceName, new JwZooKeeperClientWatcher());
			if(serviceList.isEmpty()) {
				System.out.println("3√Î∫Û‘Ÿ¥Œ≥¢ ‘");
				Thread.sleep(3000);
			
			}
			else {
				JwZooKeeperDataContainer.setServiceList(serviceList);
				Integer number=RoundRobinBalance.next(serviceList.size());
				String servericeChildName=serviceList.get(number);
				String data=jwZooKeeperOperate.readData(serviceName+"/"+servericeChildName, new JwZooKeeperClientWatcher());
				String[] hostPort=data.split(":");
				String host=hostPort[0];
				String port=hostPort[1];
				
				Test.SockerClient SockerClient= new SockerClient();
				SockerClient.send(host, port);
				break;
			}
		} else {
			jwZooKeeperOperate.createZNode(serviceName, "zbus Mq server");
		}
		}
		while(true) {
		serviceList=JwZooKeeperDataContainer.getServiceList();
		Integer number=RoundRobinBalance.next(serviceList.size());
		String servericeChildName=serviceList.get(number);
		String data=jwZooKeeperOperate.readData(serviceName+"/"+servericeChildName, new JwZooKeeperClientWatcher());
		String[] hostPort=data.split(":");
		String host=hostPort[0];
		String port=hostPort[1];
		
		Test.SockerClient SockerClient= new SockerClient();
		SockerClient.send(host, port);
		
		Thread.sleep(5000);
		}
		
		// zooKeeper.close();
	}
}