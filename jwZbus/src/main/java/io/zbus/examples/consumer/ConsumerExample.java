package io.zbus.examples.consumer;

import java.io.IOException;

import com.alibaba.fastjson.JSON;

import io.zbus.examples.rpc.biz.User;
import io.zbus.mq.Broker;
import io.zbus.mq.ConsumeGroup;
import io.zbus.mq.MessageHandler;
import io.zbus.mq.Consumer;
import io.zbus.mq.ConsumerConfig;
import io.zbus.mq.JwBusinessData;
import io.zbus.mq.Message;
import io.zbus.mq.MqClient;
import io.zbus.mq.kit.JsonKit;

//multiple instances load balancing on the same topic
//you may start many instances to check
public class ConsumerExample {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception { 
		
		Broker broker = new Broker("localhost:15555");   
		//Broker broker = new Broker("localhost:15555;localhost:15556"); //Why not test HA?
		
		ConsumerConfig config = new ConsumerConfig(broker);
		config.setTopic("bbbo");              // [R] Topic to consume
		
		ConsumeGroup group = new ConsumeGroup();
		final String project="*.account";
		final String project1="bbbo";
		group.setGroupName(project1);
		
		group.setFilter(project); 
		config.setConsumeGroup(group); 
	//	config.setConsumeGroup("jwBroad3"); // [O] ConsumeGroup, by default null, consumes the group name = topic
		config.setMessageHandler(new MessageHandler() { //Message handler, biz logic goes here
			
			public void handle(Message msg, MqClient client) throws IOException {
				// MqClient is the physical connection to MqServer, may be connected to different MqServer
				// if multiple MqServer avaialbe, with MqClient, the consumer handler may reply back, such as RPC case
				
				
				System.out.println(msg.getBodyString());
				System.out.println(msg.getJwBusinessData().getBusinessModel());
				
				System.out.println("--------------------"+project);
				
			//	System.out.println(((User)msg.getJwBusinessData().getParams().get(0)).getName());
				
			}
		});
		
		Consumer consumer = new Consumer(config);
		consumer.start(); 
	} 
}
