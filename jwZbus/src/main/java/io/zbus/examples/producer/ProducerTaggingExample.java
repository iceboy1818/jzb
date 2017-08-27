package io.zbus.examples.producer;

import io.zbus.mq.Broker;
import io.zbus.mq.Message;
import io.zbus.mq.Producer;

public class ProducerTaggingExample {

	public static void main(String[] args) throws Exception {
		Broker broker = new Broker("localhost:15555");
		// Broker broker = new Broker("localhost:15555;localhost:15556"); //Why  not test HA?

		Producer p = new Producer(broker);
		p.declareTopic("MyTopic");

		for (int i = 0; i < 1000000; i++) {
			Message msg = new Message();
			msg.setTopic("MyTopic");
			String region = "A";
			if(i%3 == 0){
				region = "HK";
			}
			String tag = String.format("Stock.%s.%06d", region, i);
			msg.setTag(tag); // tagging on message
			msg.setBody("Stock info: " + i);
			try{
				Message res = p.publish(msg);
				System.out.println(res);
			} catch (Exception e) {
				//ignore
			}

			Thread.sleep(3000);
		}

		broker.close();
	}
}
