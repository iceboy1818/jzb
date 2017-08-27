package jw.zookeeper.producer;

import java.net.InetAddress;

import jw.zookeeper.utils.JwZooKeeper;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class JwLoadBalanceProducer {

	public void pro() throws Exception {
		final JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();
		JwZooKeeper zooKeeper = new JwZooKeeper();
		zooKeeper.connect("127.0.0.1:2181");
		String rootPath = "/configcenter/";// ���ڵ�·��
		String serviceName = "serviceB";// ��������

		boolean rootExists = jwZooKeeperOperate.isExists(rootPath, new JwZooKeeperProducerWatcher());
		if (!rootExists) {
			jwZooKeeperOperate.createZNode(rootPath, null);
		}

		boolean serviceExists = jwZooKeeperOperate.isExists(rootPath + "/" + serviceName,
				new JwZooKeeperProducerWatcher());
		if (!serviceExists) {
			// ��������ڵ�
			jwZooKeeperOperate.createZNode(rootPath + "/" + serviceName, null);
		}
		// ע�ᵱǰ�������������ڽڵ�����������Žڵ��Ȩ��
		InetAddress addr = InetAddress.getLocalHost();
		String ip = addr.getHostAddress().toString();// ��ñ���IP

		// ������ǰ�������ڵ�
		jwZooKeeperOperate.createEphemeralNode(rootPath + "/" + serviceName + "/" + ip, null);
	}
}