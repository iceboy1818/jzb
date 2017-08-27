package io.zbus.examples.net;

import io.zbus.mq.Message;
import io.zbus.mq.MqClient;
import io.zbus.mq.net.EventLoop;

public class SslClientExample {
 
	public static void main(String[] args) throws Exception, InterruptedException { 
		EventLoop loop = new EventLoop(); 
		loop.setClientSslContext("ssl/zbus.crt");   
		
		MqClient client = new MqClient("localhost:15555", loop);

		Message req = new Message();
		Message res = client.invokeSync(req);
		
		System.out.println(res);
		
		
		client.close();
		loop.close();
	}

}
