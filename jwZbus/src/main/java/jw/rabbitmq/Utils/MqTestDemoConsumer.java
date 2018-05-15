package jw.rabbitmq.Utils;

public class MqTestDemoConsumer {

	public static void main(String[] args) throws Exception {
		MqCustomUtils.transactionalModeSend(1000, "test hello world", "master10", "master10Queue");
	}

}
