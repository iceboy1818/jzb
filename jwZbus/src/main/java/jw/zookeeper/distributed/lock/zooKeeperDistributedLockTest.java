package jw.zookeeper.distributed.lock;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;

import jw.zookeeper.utils.JwZooKeeper;

public class zooKeeperDistributedLockTest {

	public static void main(String[] args) throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(1);
		ZooKeeper zooKeeper = new ZooKeeper("192.168.99.100:2181", 3000, new JwZooKeeper());
		WriteLock writeLock = new WriteLock(zooKeeper, "/ro", null);
		Boolean res = writeLock.lock();
		System.out.println("get lock" + res);
		Thread.sleep(20000);
		System.out.println("sleep over");

	}

}
