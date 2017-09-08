package jw.zookeeper.distributed.lock;

public interface ZooKeeperOperation {
    
   
    public boolean execute() throws Exception;
}