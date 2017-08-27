package jw.hessian;

import java.io.IOException;
/**
 * 为了可以发送和接受这个消息持有对象，我们还需要需要一个用来序列化和反序列化的工厂
 * 需要一个hessian的子类
 * @author wangjiarong
 * 
 */
public interface InterfCodecFactory {
	/**
	 * @param obj
	 * @return 序列化数据
	 * @throws IOException
	 */
    byte[] serialize(Object obj) throws IOException;  
    
    /**
     * 
     * @param in 序列化数据
     * @return obj
     * @throws IOException
     */
    Object deSerialize(byte[] in) throws IOException;  

}
