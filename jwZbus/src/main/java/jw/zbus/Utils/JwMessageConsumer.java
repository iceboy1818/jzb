package jw.zbus.Utils;

import java.io.IOException;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import io.zbus.mq.Broker;
import io.zbus.mq.ConsumeGroup;
import io.zbus.mq.Consumer;
import io.zbus.mq.ConsumerConfig;
import io.zbus.mq.Message;
import io.zbus.mq.MessageHandler;
import io.zbus.mq.MqClient;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;
import io.zbus.mq.net.EventLoop;
import io.zbus.rpc.RpcProcessor;


/**
 * @author “Ûº—Œ∞
 * 
 */
@Service
public class JwMessageConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(JwMessageConsumer.class);
	
	@Resource(name="broker")
	Broker broker;
	
	@Value( "${topic}" )
    private String topicValue;
	
	@Value( "${broadTopic}" )
    private String broadTopicValue;
	
	@Resource(name="rpcProcessor")
	RpcProcessor rpcProcessor;
	
	public void normalModeStart(String topic,MessageHandler messageHandler){
		log.info("jwZbus normal consumer start");
		if(!StringUtils.isEmpty(topic)){
			topicValue=topic;
		}	
		log.info("jwZbus normal consumer init topic="+topicValue );
		ConsumerConfig config = new ConsumerConfig(broker);
		config.setTopic(topicValue);              
		config.setMessageHandler(messageHandler);
		Consumer consumer = new Consumer(config);
		try {
			consumer.start();
			log.info("jwZbus normal consumer start sucess");
		} catch (Exception e) {
			log.error(e.getMessage());
		} 		
	}
	
	public void rpcStart(String topic){
		log.info("jwZbus rpc consumer start");
		if(!StringUtils.isEmpty(topic)){
			topicValue=topic;
		}
		log.info("jwZbus rpc consumer init topic="+topicValue );
		ConsumerConfig config = new ConsumerConfig(broker); 
		config.setTopic(topicValue);
		config.setMessageHandler(rpcProcessor);   
		Consumer consumer = new Consumer(config); 
		try {
			consumer.start();
			log.info("jwZbus rpc consumer start sucess");
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
	
	public void broadStart(String broadTopic,String projectName,MessageHandler messageHandler){
		log.info("jwZbus broad consumer start");
			ConsumerConfig config = new ConsumerConfig(broker);
			
			if(!StringUtils.isEmpty(broadTopic)){
				broadTopicValue=broadTopic;
			}
			log.info("jwZbus broad consumer init topic="+topicValue +"  broadTopic="+broadTopicValue);
			config.setTopic(broadTopicValue);  
			ConsumeGroup consumeGroup = new ConsumeGroup();
			consumeGroup.setGroupName(broadTopicValue + projectName);
			consumeGroup.setFilter(projectName);
			config.setConsumeGroup(consumeGroup); //ConsumeGroup name 
			config.setConnectionCount(1);            //Demo only 1 connection for each consumer
			config.setMessageHandler(messageHandler);
			Consumer consumer = new Consumer(config);
			try {
				consumer.start();
				log.info("jwZbus broad consumer start sucess");
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		
			
	}
}
