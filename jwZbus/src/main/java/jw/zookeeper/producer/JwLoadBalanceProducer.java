package jw.zookeeper.producer;

import java.net.InetAddress;

import jw.zookeeper.utils.JwZooKeeper;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class JwLoadBalanceProducer {

	public void pro() throws Exception {
		final JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();
		JwZooKeeper zooKeeper = new JwZooKeeper();
		zooKeeper.connect("127.0.0.1:2181");
		String rootPath = "/configcenter/";// 根节点路径
		String serviceName = "serviceB";// 服务名称

		boolean rootExists = jwZooKeeperOperate.isExists(rootPath, new JwZooKeeperProducerWatcher());
		if (!rootExists) {
			jwZooKeeperOperate.createZNode(rootPath, null);
		}

		boolean serviceExists = jwZooKeeperOperate.isExists(rootPath + "/" + serviceName,
				new JwZooKeeperProducerWatcher());
		if (!serviceExists) {
			// 创建服务节点
			jwZooKeeperOperate.createZNode(rootPath + "/" + serviceName, null);
		}
		// 注册当前服务器，可以在节点的数据里面存放节点的权重
		InetAddress addr = InetAddress.getLocalHost();
		String ip = addr.getHostAddress().toString();// 获得本机IP

		// 创建当前服务器节点
		jwZooKeeperOperate.createEphemeralNode(rootPath + "/" + serviceName + "/" + ip, null);
	}
}