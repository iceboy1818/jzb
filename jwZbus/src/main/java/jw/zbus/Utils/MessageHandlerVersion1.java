package jw.zbus.Utils;

import java.io.IOException;

import io.zbus.mq.Message;
import io.zbus.mq.MessageHandler;
import io.zbus.mq.MqClient;

public class MessageHandlerVersion1 implements MessageHandler{

	
	public void handle(Message msg, MqClient client) throws IOException {
		// MqClient is the physical connection to MqServer, may be connected to different MqServer
		// if multiple MqServer avaialbe, with MqClient, the consumer handler may reply back, such as RPC case
		
		System.out.println(msg);
	}

}
