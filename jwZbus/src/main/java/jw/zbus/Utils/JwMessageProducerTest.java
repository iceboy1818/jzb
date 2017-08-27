package jw.zbus.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.zbus.examples.rpc.biz.User;
import io.zbus.mq.JwBusinessData;



public class JwMessageProducerTest {
	
	public static void main(String[] args){
		
		ApplicationContext context1 = new ClassPathXmlApplicationContext("spring-zbus.xml");
		JwMessageProducer	 innjiaMessageProducer =(JwMessageProducer)context1.getBean("jwMessageProducer"); 
		JwBusinessData jwBusinessData=new JwBusinessData();
		jwBusinessData.setBusinessModel("jwBusinessType");
		User User= new User();
		User.setName("po");
		Map<String,Object> params= new HashMap<String,Object>();
		params.put("user", User);
		jwBusinessData.setParams(params);
		for(int i=0;i<10;i++){
		innjiaMessageProducer.send(jwBusinessData, null);
		}
		
		
	}
}
