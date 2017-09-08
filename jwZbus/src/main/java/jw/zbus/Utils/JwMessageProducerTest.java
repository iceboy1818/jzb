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
		User.setName("ppppppppo");
		Map<String,Object> params= new HashMap<String,Object>();
		params.put("user", User);
		jwBusinessData.setDatas(params);
		
		innjiaMessageProducer.broadSend(jwBusinessData, null, "a.account.b");
		
		
		
	}
}
