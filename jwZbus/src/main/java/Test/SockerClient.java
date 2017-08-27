package Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SockerClient {
	
	public void send(String host, String port) {
		System.out.println("Client Start..." + host + ":" + port);

		Socket socket = null;
		try {
			// ����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�
			socket = new Socket(host, Integer.valueOf(port));

			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			PrintStream out = new PrintStream(socket.getOutputStream());

			out.println("hello");

			String ret = input.readLine();
			System.out.println("�������˷��ع�������: " + ret);
			out.println("hello1");
			out.close();
			input.close();
		} catch (Exception e) {
			System.out.println("�ͻ����쳣:" + e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					socket = null;
					System.out.println("�ͻ��� finally �쳣:" + e.getMessage());
				}
			}

		}
	}

}
