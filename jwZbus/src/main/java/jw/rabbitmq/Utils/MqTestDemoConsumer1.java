package jw.rabbitmq.Utils;

public class MqTestDemoConsumer1 {

	public static void main(String[] args) throws Exception {
		MqCustomUtils.consume1("masterQueue", new MqCustomConsumer() {
			public	void processConsume(Object message) {
				System.out.println(message);
			}
		});
		

	}
	
}
