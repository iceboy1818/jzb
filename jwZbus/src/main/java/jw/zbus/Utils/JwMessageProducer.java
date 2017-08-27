package jw.zbus.Utils;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.zbus.examples.rpc.biz.IBaseExt;
import io.zbus.examples.rpc.biz.InterfaceExample;
import io.zbus.mq.Broker;
import io.zbus.mq.JwBusinessData;
import io.zbus.mq.Message;
import io.zbus.mq.Producer;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import io.zbus.rpc.RpcInvoker;
import jw.zbus.exception.JwMessageException;


/**
 * @author “Ûº—Œ∞
 * 
 */
@Service
public class JwMessageProducer {
	
	private static final Logger log = LoggerFactory.getLogger(JwMessageProducer.class);
	
	
	@Resource(name="broker")
	Broker broker ;
	
	@Resource(name="rpcInvoker")
	RpcInvoker rpcInvoker;
	
	@Resource(name="interfaceExample")
	InterfaceExample interfaceExample;
	
	//@Resource(name="jwMongoTemplate")
	//MongoTemplate mongoTemplate;
	
	@Resource(name="iBaseExt")
	IBaseExt iBaseExt;
	
	@Value( "${topic}" )
    private String topicValue;
	
	@Value( "${broadTopic}" )
    private String broadTopicValue;

	public void send(JwBusinessData jwBusinessData,String topic) {
		log.info("jwZbus send jwMessage start");
		if(!StringUtils.isEmpty(topic)){
			topicValue=topic;
		}		
		log.info("jwZbus send jwMessage init topic="+topicValue );
		Producer p = new Producer(broker);  
		try {
			p.declareTopic(topicValue); 
			 
			Message msg = new Message();
			
			msg.setTopic(topicValue);     
			
			msg.setJwBusinessData(jwBusinessData); 
			log.info("jwZbus send jwMessage init param="+msg.getBodyString() );
			Message res = p.publish(msg);
			log.info("jwZbus send jwMessage sucess" ); 
		//	mongoTemplate.save(new jw.mongodb.model.User("sucess"));
		}  catch (Exception e) { 
			log.error(e.getMessage());
			throw new JwMessageException(e.getMessage());
		} 
	}
	
	
	public void broadSend(JwBusinessData jwBusinessData,String topic) {
		log.info("jwZbus broadSend jwMessage start");
		if(!StringUtils.isEmpty(topic)){
			broadTopicValue=topic;
		}		
		log.info("jwZbus broadSend jwMessage init topic="+broadTopicValue );
		Producer p = new Producer(broker);  
		try {
			p.declareTopic(broadTopicValue); 
			 
			Message msg = new Message();
			
			msg.setTopic(broadTopicValue);     
			
			msg.setJwBusinessData(jwBusinessData); 
			log.info("jwZbus broadSend jwMessage init param="+msg.getBodyString() );
			Message res = p.publish(msg);
			log.info("jwZbus broadSend jwMessage sucess" ); 
		//	mongoTemplate.save(new jw.mongodb.model.User("sucess"));
		}  catch (Exception e) { 
			log.error(e.getMessage());
			throw new JwMessageException(e.getMessage());
		} 
	}
	
//	public void rpcInvoke(){
//		User user= new User();
//		user.setAge(1);
//		user.setName("yinjiawei");
//		User result= iBaseExt.save(user);
//		
//	}
}
