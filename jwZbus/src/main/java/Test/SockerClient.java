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
			// 创建一个流套接字并将其连接到指定主机上的指定端口号
			socket = new Socket(host, Integer.valueOf(port));

			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			PrintStream out = new PrintStream(socket.getOutputStream());

			out.println("hello");

			String ret = input.readLine();
			System.out.println("服务器端返回过来的是: " + ret);
			out.println("hello1");
			out.close();
			input.close();
		} catch (Exception e) {
			System.out.println("客户端异常:" + e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					socket = null;
					System.out.println("客户端 finally 异常:" + e.getMessage());
				}
			}

		}
	}

}
