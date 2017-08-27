package jw.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
/**
 * 下面是编码解码的实现类，用了hessian来实现，大家可以自行选择序列化方式
 * 把需要的数据放到模型中传输
 */
public class HessianCodecFactory implements InterfCodecFactory {  

	/**
	 * logger, 负责输出log
	 */
    private final Logger logger = Logger.getLogger(HessianCodecFactory.class);  
  
    /*
     * (non-Javadoc)
     * @see com.databus.service.codec.InterfCodecFactory#serialize(java.lang.Object)
     * 序列化函数
     * 
     */
    public byte[] serialize(Object obj) throws IOException {  
        HessianOutput output = null;  
        ByteArrayOutputStream baos = null;  
        try {  
            /**
             * it will automatically increase 337 or more 2048
             */
            baos = new ByteArrayOutputStream(48);
            output = new HessianOutput(baos);  
            output.startCall();  
            output.writeObject(obj);  
            output.completeCall();  
        } catch (final IOException ex) {  
            throw ex;  
        } finally {  
            if (output != null) {  
                try {  
                    baos.close();  
                } catch (final IOException ex) {  
                    this.logger.error("Failed to close stream.", ex);  
                }  
            }  
        }  
        return baos != null ? baos.toByteArray() : null;  
    }  
    /*
     * (non-Javadoc)
     * @see com.databus.service.codec.InterfCodecFactory#deSerialize(byte[])
     * 反序列化
     */
    public Object deSerialize(byte[] in) throws IOException {  
        Object obj = null;  
        ByteArrayInputStream bais = null;  
        HessianInput input = null;  
        try {  
            bais = new ByteArrayInputStream(in);  
            input = new HessianInput(bais);  
            input.startReply();  
            obj = input.readObject();  
            input.completeReply();  
        } catch (final IOException ex) {  
            throw ex;  
        } catch (final Throwable e) {  
            this.logger.error("Failed to decode object.", e);  
        } finally {  
            if (input != null) {  
                try {  
                    bais.close();  
                } catch (final IOException ex) {  
                    this.logger.error("Failed to close stream.", ex);  
                }  
            }  
        }  
        return obj;  
    }  
  
}  