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
	 * 删除一个zMode节点, void delete(path<节点路径>, stat<数据版本号>)
	 * </p>
	 * <br/>
	 * 
	 * <pre>
	 *  
	 *     说明 
	 *     1、版本号不一致,无法进行数据删除操作. 
	 *     2、如果版本号与znode的版本号不一致,将无法删除,是一种乐观加锁机制;如果将版本号设置为-1,不会去检测版本,直接删除.
	 * </pre>
	 * 
	 * @param path
	 *            zNode节点路径
	 * @return 删除成功返回true,反之返回false.
	 */
	public boolean deteleZNode(String path) {
		try {
			JwZooKeeper.zooKeeper.delete(path, -1);
			logger.info("ZooKeeper删除节点成功，节点地址：" + path);
			return true;
		} catch (InterruptedException e) {
			logger.error("删除节点失败：" + e.getMessage() + "，path:" + path, e);
		} catch (KeeperException e) {
			logger.error("删除节点失败：" + e.getMessage() + "，path:" + path, e);
		}
		return false;
	}

	/**
	 * <p>
	 * 更新指定节点数据内容, Stat setData(path<节点路径>, data[]<节点内容>, stat<数据版本号>)
	 * </p>
	 * 
	 * <pre>
	 *  
	 *     设置某个znode上的数据时如果为-1，跳过版本检查
	 * </pre>
	 * 
	 * @param path
	 *            zNode节点路径
	 * @param data
	 *            zNode数据内容
	 * @return 更新成功返回true,返回返回false
	 */
	public boolean updateZNodeData(String path, String data) {
		try {
			Stat stat = JwZooKeeper.zooKeeper.setData(path, data.getBytes(), -1);
			logger.info("更新数据成功, path：" + path + ", stat: " + stat);
			return true;
		} catch (KeeperException e) {
			logger.error("更新节点数据失败：" + e.getMessage() + "，path:" + path, e);
		} catch (InterruptedException e) {
			logger.error("更新节点数据失败：" + e.getMessage() + "，path:" + path, e);
		}
		return false;
	}

	/**
	 * <p>
	 * 读取指定节点数据内容,byte[] getData(path<节点路径>, watcher<监视器>, stat<数据版本号>)
	 * </p>
	 * 
	 * @param path
	 *            zNode节点路径
	 * @return 节点存储的值,有值返回,无值返回null
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
			
			logger.info("读取数据成功, path:" + path + ", content:" + data);
		} catch (KeeperException e) {
			logger.error("读取数据失败,发生KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("读取数据失败,发生InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
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
				logger.info("中没有节点" + path);
			}
			return list;
		} catch (KeeperException e) {
			logger.error("读取子节点数据失败,发生KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("读取子节点数据失败,发生InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
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
			logger.error("读取数据失败,发生KeeperException! path: " + path + ", errMsg:" + e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error("读取数据失败,发生InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e);
		}
		return false;
	}
}