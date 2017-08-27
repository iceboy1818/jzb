package Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import jw.zookeeper.client.JwZooKeeperClientWatcher;
import jw.zookeeper.utils.JwZooKeeper;
import jw.zookeeper.utils.JwZooKeeperOperate;

public class SocketServer {

	public void start(String port, String serverName) {
		System.out.println("Server...\n"+ serverName+":"+ "port");
		SocketServer server = new SocketServer();
		try {
			// 创建一个ServerSocket，这里可以指定连接请求的队列长度
			// new ServerSocket(port,3);意味着当队列中有3个连接请求是，如果Client再请求连接，就会被Server拒绝
			ServerSocket serverSocket = new ServerSocket(Integer.valueOf(port));

			JwZooKeeper jwZooKeeper = new JwZooKeeper();
			jwZooKeeper.connect("192.168.99.100:2183");
			String serviceName = "/jwZbus";
			JwZooKeeperOperate jwZooKeeperOperate = new JwZooKeeperOperate();
			Boolean serviceNameExistsFlag = jwZooKeeperOperate.isExists(serviceName, new JwZooKeeperClientWatcher());
			if (serviceNameExistsFlag) {
				;
			} else {
				jwZooKeeperOperate.createZNode(serviceName, "zbus Mq server");
			}
			jwZooKeeperOperate.createEphemeralNode(serviceName+"/"+serverName, "127.0.0.1:" + port);

			while (true) {
				// 从请求队列中取出一个连接
				Socket client = serverSocket.accept();
				// 处理这次连接
				new HandlerThread(client);
			}
		} catch (Exception e) {
			System.out.println("服务器异常: " + e.getMessage());
		}
	}

	private class HandlerThread implements Runnable {
		private Socket socket;

		public HandlerThread(Socket client) {
			socket = client;
			new Thread(this).start();
		}

		public void run() {
			try {
				// 读取客户端数据
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientInputStr = input.readLine();// 这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
				// 处理客户端数据
				System.out.println("客户端发过来的内容:" + clientInputStr);

				// 向客户端回复信息
				PrintStream out = new PrintStream(socket.getOutputStream());

				out.println("helloBack");

				out.close();
				input.close();
			} catch (Exception e) {
				System.out.println("服务器 run 异常: " + e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						socket = null;
						System.out.println("服务端 finally 异常:" + e.getMessage());
					}
				}
			}
		}
	}
}
