package jw.zbus.Utils;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.zbus.examples.rpc.biz.User;
import io.zbus.mq.Message;
import io.zbus.mq.MessageHandler;
import io.zbus.mq.MqClient;


public class JwMessageConsumerTest {
 public static void main(String[] args) throws Exception{
	
		ClassPathXmlApplicationContext context1 = new ClassPathXmlApplicationContext("spring-zbus.xml");
		JwMessageConsumer jwMessageConsumer =(JwMessageConsumer)context1.getBean("jwMessageConsumer"); 
		jwMessageConsumer.broadStart( null, "zhsh", new MessageHandler() {
			
			
			public void handle(Message msg, MqClient client) throws IOException {
				System.out.println(msg.getBodyString());
				System.out.println(msg.getJwBusinessData().getBusinessModel());
				System.out.println(((User)msg.getJwBusinessData().getParams().get("user")).getName());
				
			}
		});
	 
 }
}