package jw.redis.utils;

import java.io.ByteArrayInputStream;  
import java.io.ByteArrayOutputStream;  
import java.io.Closeable;  
import java.io.IOException;  
import java.io.ObjectInputStream;  
import java.io.ObjectOutputStream;  
  
import com.alibaba.fastjson.JSONObject;

import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import jw.zbus.Utils.JwMessageConsumer; 
  
public class SerializeUtil {  
	private static final Logger log = LoggerFactory.getLogger(SerializeUtil.class);
	
	static final Class<?> CLAZZ = SerializeUtil.class;  
      
    public static byte[] serialize(Object value) {  
        if (value == null) {   
            throw new NullPointerException("Can't serialize null");  
        }  
        byte[] rv = null;  
        ByteArrayOutputStream bos = null;  
        ObjectOutputStream os = null;  
        try {  
            bos = new ByteArrayOutputStream();  
            os = new ObjectOutputStream(bos);  
            os.writeObject(value);  
            os.close();  
            bos.close();  
            rv = bos.toByteArray();  
        } catch (Exception e) {  
        	log.error( JSONObject.toJSONString(value));  
        } finally {  
            close(os);  
            close(bos);  
        }  
        return rv;  
    }  
  
      
    public static Object deserialize(byte[] in) {  
        return deserialize(in, Object.class);  
    }  
  
    public static <T> T deserialize(byte[] in, Class<T>...requiredType) {  
        Object rv = null;  
        ByteArrayInputStream bis = null;  
        ObjectInputStream is = null;  
        try {  
            if (in != null) {  
                bis = new ByteArrayInputStream(in);  
                is = new ObjectInputStream(bis);  
                rv = is.readObject();  
            }  
        } catch (Exception e) {  
             log.error(e.getMessage()); 
        } finally {  
            close(is);  
            close(bis);  
        }  
        return (T) rv;  
    }  
  
    private static void close(Closeable closeable) {  
        if (closeable != null)  
            try {  
                closeable.close();  
            } catch (IOException e) {  
                 log.error(e.getMessage());
            }  
    }  
  
}  