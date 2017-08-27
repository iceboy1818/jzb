package jw.rabbitmq.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

import jw.hessian.HessianCodecFactory;

public class MqCustomUtils {

	private static Lock lock = new ReentrantLock();

	private static ConnectionFactory connectionFactory;

	public static Connection connection;
	
	public static HessianCodecFactory hessianCodecFactory;

	private static Connection getMqConnection() throws Exception {

		if (connection == null) {
			lock.lock();
			if (connection == null) {
				connectionFactory = new ConnectionFactory();
				initConnectionFactory(connectionFactory);
				hessianCodecFactory=new HessianCodecFactory();
				connection = connectionFactory.newConnection();
			}
			lock.unlock();
		}
		return connection;
	}

	private static void initConnectionFactory(ConnectionFactory connectionFactory) {
		ResourceBundle conf = ResourceBundle.getBundle("mqServerInfo");
		String userName = conf.getString("userName");
		String password = conf.getString("password");
		String virtualHost = conf.getString("virtualHost");
		String host = conf.getString("host");
		String port = conf.getString("port");

//		connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(userName);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setHost(host);
		connectionFactory.setPort(Integer.valueOf(port));
	}

	public static Channel getMqChannel() throws Exception {
		return getMqConnection().createChannel();
	}

	public static void closeConnecton(Connection connection) throws Exception {
		connection.close();
	}

	public static Boolean confirmModeSend(ConfirmListener confirmListener, Integer timeout, String message,
			String exchangeName, String queue) throws Exception {
		Channel channel = getMqChannel();
		channel.confirmSelect();
		channel.addConfirmListener(confirmListener);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("x-message-ttl", 60000);
		String deathBox = "deathBox";
		map.put("x-dead-letter-exchange", "deathBox");
		map.put("x-dead-letter-routing-key", deathBox + "RoutKey");
		channel.exchangeDeclare(exchangeName, "direct", true);
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, exchangeName, exchangeName + "RoutKey");
		// BasicProperties basicProperties= new BasicProperties();
		// basicProperties.builder().deliveryMode(DeliveryMode.PERSISTENT);
		byte[] messageBodyBytes = message.getBytes();
		Date d = new Date();
		System.out.println(d);

		channel.basicPublish(exchangeName, exchangeName + "RoutKey", MessageProperties.PERSISTENT_TEXT_PLAIN,
				messageBodyBytes);

		Date d2 = new Date();
		System.out.println(d2);
		boolean isok = channel.waitForConfirms();
		System.out.println("sucess");
		System.out.println("sucess " + isok);
		channel.close();
		return true;
	}
	
	

	public static Boolean confirmModeSend(ConfirmListener confirmListener, Integer timeout, Object message,
			String exchangeName, String queue) throws Exception {
		Channel channel = getMqChannel();
		channel.confirmSelect();
		channel.addConfirmListener(confirmListener);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("x-message-ttl", 60000);
		String deathBox = "deathBox";
		map.put("x-dead-letter-exchange", "deathBox");
		map.put("x-dead-letter-routing-key", deathBox + "RoutKey");
		channel.exchangeDeclare(exchangeName, "direct", true);
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, exchangeName, exchangeName + "RoutKey");
		// BasicProperties basicProperties= new BasicProperties();
		// basicProperties.builder().deliveryMode(DeliveryMode.PERSISTENT);
		byte[] messageBodyBytes=hessianCodecFactory.serialize(message);
		Date d = new Date();
		System.out.println(d);

		channel.basicPublish(exchangeName, exchangeName + "RoutKey", MessageProperties.PERSISTENT_TEXT_PLAIN,
				messageBodyBytes);

		Date d2 = new Date();
		System.out.println(d2);
		boolean isok = channel.waitForConfirms();
		System.out.println("sucess");
		System.out.println("sucess " + isok);
		channel.close();
		return true;
	}
	public static Boolean transactionalModeSend(Integer timeout, String message, String exchangeName, String queue)
			throws Exception {
		Channel channel = getMqChannel();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("x-message-ttl", 60000);
		String deathBox = "deathBox";
		map.put("x-dead-letter-exchange", "deathBox");
		map.put("x-dead-letter-routing-key", deathBox + "RoutKey");
		channel.exchangeDeclare(exchangeName, "direct", true);
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, exchangeName, exchangeName + "RoutKey");
		// BasicProperties basicProperties= new BasicProperties();
		// basicProperties.builder().deliveryMode(DeliveryMode.PERSISTENT);
		byte[] messageBodyBytes = message.getBytes();
		Date d = new Date();
		System.out.println(d);

		channel.txSelect();
		channel.basicPublish(exchangeName, exchangeName + "RoutKey", MessageProperties.PERSISTENT_TEXT_PLAIN,
				messageBodyBytes);
		channel.txCommit();

		Date d2 = new Date();
		System.out.println(d2);
		boolean isok = channel.waitForConfirms();
		System.out.println("sucess");
		System.out.println("sucess " + isok);
		channel.close();
		return true;
	}

	public static Boolean consume(String queueName,final MqCustomConsumer mqCustomConsumer) throws Exception {
		final Channel channel = getMqChannel();
		channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				Object message=hessianCodecFactory.deSerialize(body);
				// String routingKey = envelope.getRoutingKey();
				// String contentType = properties.getContentType();
				mqCustomConsumer.processConsume(message);
				
				long deliveryTag = envelope.getDeliveryTag();
				System.out.println(new String(body));
				channel.basicAck(deliveryTag, false);
				System.out.println("consumer sucess one task");
			}
		});
		System.out.println("consumer receive one task");

		
		return true;
	}
	
	
}
