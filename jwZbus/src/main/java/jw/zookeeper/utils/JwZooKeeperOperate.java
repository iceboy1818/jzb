package jw.zookeeper.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import jw.zookeeper.client.JwZooKeeperClientWatcher;

public class JwZooKeeperOperate {

	Logger logger = Logger.getLogger(JwZooKeeperOperate.class);

	public boolean createZNode(String path, String data) {
		try {
			String zkPath = JwZooKeeper.zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			return true;
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createEphemeralNode(String path, String data) {
		try {
			String zkPath = JwZooKeeper.zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);

			return true;
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * <p>
	 * ɾ��һ��zMode�ڵ�, void delete(path<�ڵ�·��>, stat<���ݰ汾��>)
	 * </p>
	 * <br/>
	 * 
	 * <pre>
	 *  
	 *     ˵�� 
	 *     1���汾�Ų�һ��,�޷���������ɾ������. 
	 *     2������汾����znode�İ汾�Ų�һ��,���޷�ɾ��,��һ���ֹۼ�������;������汾������Ϊ-1,����ȥ���汾,ֱ��ɾ��.
	 * </pre>
	 * 
	 * @param path
	 *            zNode�ڵ�·��
	 * @return ɾ���ɹ�����true,��֮����false.
	 */
	public boolean deteleZNode(String path) {
		try {
			JwZooKeeper.zooKeeper.delete(path, -1);
			logger.info("ZooKeeperɾ���ڵ�ɹ����ڵ��ַ��" + path);
			return true;
		} catch (InterruptedException e) {
			logger.error("ɾ���ڵ�ʧ�ܣ�" + e.getMessage() + "��path:" + path, e);
		} catch (KeeperException e) {
			logger.error("ɾ���ڵ�ʧ�ܣ�" + e.getMessage() + "��path:" + path, e);
		}
		return false;
	}

	/**
	 * <p>
	 * ����ָ���ڵ���������, Stat setData(path<�ڵ�·��>, data[]<�ڵ�����>, stat<���ݰ汾��>)
	 * </p>
	 * 
	 * <pre>
	 *  
	 *     ����ĳ��znode�ϵ�����ʱ���Ϊ-1�������汾���
	 * </pre>
	 * 
	 * @param path
	 *            zNode�ڵ�·��
	 * @param data
	 *            zNode��������
	 * @return ���³ɹ�����true,���ط���false
	 */
	public boolean updateZNodeData(String path, String data) {
		try {
			Stat stat = JwZooKeeper.zooKeeper.setData(path, data.getBytes(), -1);
			logger.info("�������ݳɹ�, path��" + path + ", stat: " + stat);
			return true;
		} catch (KeeperException e) {
			logger.error("���½ڵ�����ʧ�ܣ�" + e.getMessage() + "��path:" + path, e);
		} catch (InterruptedException e) {
			logger.error("���½ڵ�����ʧ�ܣ�" + e.getMessage() + "��path:" + path, e);
		}
		return false;
	}

	/**
	 * <p>
	 * ��ȡָ���ڵ���������,byte[] getData(path<�ڵ�·��>, watcher<������>, stat<���ݰ汾��>)
	 * </p>
	 * 
	 * @param path
	 *            zNode�ڵ�·��
	 * @return �ڵ�洢��ֵ,��ֵ����,��ֵ����null
	 */
	public String readData(String path,Watcher watcher) {
		String data = null;
		try {
			
			if (watcher == null) {
				data = new String(JwZooKeeper.zooKeeper.getData(path, true,null));
			}
			else {
				data = new String(JwZooKeeper.zooKeeper.getData(path, watcher, null));
			}
			
			logger.info("��ȡ���ݳɹ�, path:" + path + ", content:" + data);
		} catch (KeeperException e) {
			logger.error("��ȡ����ʧ��,����KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("��ȡ����ʧ��,����InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
		}
		return data;
	}

	
	public List<String> getChildren(String path,Watcher watcher) {
		try {
			List<String> list=null;
			if (watcher == null) {
				 list = JwZooKeeper.zooKeeper.getChildren(path, true);
			}
			else {
				 list = JwZooKeeper.zooKeeper.getChildren(path,watcher);
			}
			if (list.isEmpty()) {
				logger.info("��û�нڵ�" + path);
			}
			return list;
		} catch (KeeperException e) {
			logger.error("��ȡ�ӽڵ�����ʧ��,����KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("��ȡ�ӽڵ�����ʧ��,����InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
		}
		return null;
	}

	
	
	public boolean isExists(String path, Watcher watcher) {
		try {
			Stat stat=null;
			if (watcher == null) {
				 stat = JwZooKeeper.zooKeeper.exists(path, true);
			}
			else {
				 stat = JwZooKeeper.zooKeeper.exists(path, watcher);
			}
			
			return null != stat;
		} catch (KeeperException e) {
			logger.error("��ȡ����ʧ��,����KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("��ȡ����ʧ��,����InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
		}
		return false;
	}
}