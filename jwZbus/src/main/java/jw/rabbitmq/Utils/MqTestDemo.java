package jw.rabbitmq.Utils;

public class MqTestDemo {

	public static void main(String[] args) throws Exception{
		//MqCustomUtils.transactionalModeSend(1000, "test hello world", "testExchange", "testQueue");
		MqCustomUtils.consume("master10Queue", new MqCustomConsumer() {
			public	void processConsume(Object message) {
				System.out.println(message);
			}
		});
//		MqCustomUtils.consume1("testQueue", new MqCustomConsumer() {
//			public	void processConsume(Object message) {
//				System.out.println(message);
//			}
//		});
		
	}

}
