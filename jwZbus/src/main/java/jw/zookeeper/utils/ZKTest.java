 package jw.zookeeper.utils;



public class ZKTest {

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10; i++) {
			final int index = i;
			new Thread() {
				@Override
				public void run() {
					DistributedLock lock = null;
					try {
						lock = new DistributedLock("AAA");
						lock.lock();
						System.out.println(index + "==Thread " + Thread.currentThread().getId() + " running");
					//	Log.i();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
//						try {
//						//	Thread.sleep(3000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						lock.unlock();
						System.out.println(index + "==Thread " + Thread.currentThread().getId() + " unlock");
					}
				}
			}.start();
		}
	}
}