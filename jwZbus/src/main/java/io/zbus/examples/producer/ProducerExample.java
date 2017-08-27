package io.zbus.examples.producer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.zbus.examples.rpc.biz.User;
import io.zbus.mq.Broker;
import io.zbus.mq.JwBusinessData;
import io.zbus.mq.Message;
import io.zbus.mq.Producer; 

public class ProducerExample { 
	
	public static void main(String[] args) throws Exception { 
		Broker broker = new Broker("localhost:15555"); 
		//Broker broker = new Broker("localhost:15555;localhost:15556"); //Why not test HA?
		  
		Producer p = new Producer(broker);  //Producer is lightweight Java object, no need to destory
		p.declareTopic("bbbo"); //If topic not exists, declare it 
		 
		Message msg = new Message();
		msg.setTopic("bbbo");     // [R] required, a message needs Topic 
		JwBusinessData jwBusinessData=new JwBusinessData();
		jwBusinessData.setBusinessModel("898");
		User User= new User();
		User.setName("555");
		Map<String,Object> params= new HashMap<String,Object>();
		params.put("user", User);
		jwBusinessData.setParams(params);
		msg.setJwBusinessData(jwBusinessData);
		  //a binary blob, application may define it's own format
		msg.setTag("a.accountb"); 
		Message res = p.publish(msg);//Synchroneous, for async:  p.pubglishAsync(msg, callback);
		
		System.out.println(res);    
		
		
		broker.close(); //Broker is a heavey Java Object, it should be shared, and has to be destroyed
	}
}
