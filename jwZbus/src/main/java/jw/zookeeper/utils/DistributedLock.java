package jw.zookeeper.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


public class DistributedLock implements Lock, Watcher {
	private static ZooKeeper zk;
	private String root = "/zookeeper";// 鏍�
	private String lockName;// 绔炰簤璧勬簮鐨勬爣蹇�
	private String waitNode;// 绛夊緟鍓嶄竴涓攣
	private String myZnode;// 褰撳墠閿�
	private CountDownLatch latch;// 璁℃暟鍣�
	private int sessionTimeout = 3000;
	private static String config = "192.168.99.100:2181";
	
	// static {
	// config = PropertiesUtils.getValueByKey("zookeeper.url");
	// }

	public String getWaitNode() {
		return waitNode;
	}


	public void setWaitNode(String waitNode) {
		this.waitNode = waitNode;
	}


	public String getMyZnode() {
		return myZnode;
	}


	public void setMyZnode(String myZnode) {
		this.myZnode = myZnode;
	}


	/**
	 * 鍒涘缓鍒嗗竷寮忛攣,浣跨敤鍓嶈纭config閰嶇疆鐨剒ookeeper鏈嶅姟鍙敤
	 * 
	 * @param config
	 *            127.0.0.1:2181
	 * @param lockName
	 *            绔炰簤璧勬簮鏍囧織,lockName涓笉鑳藉寘鍚崟璇峫ock
	 * @throws IOException
	 */
	private synchronized static void createZookeeper(DistributedLock o)
			throws IOException {

			zk = new ZooKeeper(config, 120000, o);
		}
	

	public DistributedLock(String lockName) {
		this.lockName = lockName;
		// 鍒涘缓涓�涓笌鏈嶅姟鍣ㄧ殑杩炴帴
		try {
			createZookeeper(this);
			Stat stat = zk.exists(root, false);
			if (stat == null) {
				// 鍒涘缓鏍硅妭鐐�
				zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lock() {
		try {
			if (this.tryLock()) {
				
				return;
			} else {
				waitForLock(waitNode, sessionTimeout);// 绛夊緟閿�
			}
		} catch (KeeperException e) {
			throw new LockException(e);
		} catch (InterruptedException e) {
			throw new LockException(e);
		}
	}
	@Override
	public boolean tryLock() {
		try {
			String splitStr = "_lock_";
			if (lockName.contains(splitStr))
				throw new LockException("閿佺殑鍚嶇О涓嶈兘鍖呭惈lock");
			// 鍒涘缓涓存椂瀛愯妭鐐�
			createTmpNode(splitStr);
			// 鍙栧嚭鎵�鏈夊瓙鑺傜偣
			List<String> subNodes = zk.getChildren(root, false);
			// 鍙栧嚭鎵�鏈塴ockName鐨勯攣
			List<String> lockObjNodes = new ArrayList<String>();
			for (String node : subNodes) {
				String _node = node.split(splitStr)[0];
				if (_node.equals(lockName)) {
					lockObjNodes.add(node);
				}
			}
			Collections.sort(lockObjNodes);
			if (myZnode.equals(root + "/" + lockObjNodes.get(0))) {
				// 濡傛灉鏄渶灏忕殑鑺傜偣,鍒欒〃绀哄彇寰楅攣
				return true;
			}
			// 濡傛灉涓嶆槸鏈�灏忕殑鑺傜偣锛屾壘鍒版瘮鑷繁灏�1鐨勮妭鐐�
			String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
			waitNode = lockObjNodes.get(
					Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
			//System.out.println(myZnode+"watch"+waitNode);
		} catch (KeeperException e) {
			throw new LockException(e);
		} catch (InterruptedException e) {
			throw new LockException(e);
		}
		return false;
	}

	private void createTmpNode(String splitStr)
			throws KeeperException, InterruptedException {
		myZnode = zk.create(root + "/" + lockName + splitStr, new byte[0],
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	}
	private boolean waitForLock(String lower, long waitTime)
			throws InterruptedException, KeeperException {
		Stat stat = zk.exists(root + "/" + lower, this);
		if(stat==null){
			System.out.println("------------------------------------前一个节点"+lower+"已经删除");
		}
		System.out.println(this.toString() +"|"+myZnode+"reallywatch" + "/" + lower);
		// 鍒ゆ柇姣旇嚜宸卞皬娉ㄥ唽涓�涓暟鐨勮妭鐐规槸鍚﹀瓨鍦�,濡傛灉涓嶅瓨鍦ㄥ垯鏃犻渶绛夊緟閿�,鍚屾椂鐩戝惉
		if (stat != null) {
			this.latch = new CountDownLatch(1);
			this.latch.await(waitTime, TimeUnit.MILLISECONDS);
			this.latch = null;
		}
		return true;
	}
	/**
	 * zookeeper鑺傜偣鐨勭洃瑙嗗櫒
	 */
	@Override
	public void process(WatchedEvent event) {
		System.out.println(this.toString()+myZnode+"前一个被删除"+waitNode+event.getType()+	"|"+event.getPath());
	
		if (this.latch != null) {
			this.latch.countDown();
			
		}
	}
	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		try {
			if (this.tryLock()) {
				return true;
			}
			return waitForLock(waitNode, time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void unlock() {
		try {
			
			zk.delete(myZnode, -1);
			System.out.println(myZnode+"deleted");
			myZnode = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		this.lock();
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	public class LockException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public LockException(String e) {
			super(e);
		}
		public LockException(Exception e) {
			super(e);
		}
	}

}