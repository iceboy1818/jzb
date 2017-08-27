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
			// ����һ��ServerSocket���������ָ����������Ķ��г���
			// new ServerSocket(port,3);��ζ�ŵ���������3�����������ǣ����Client���������ӣ��ͻᱻServer�ܾ�
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
				// �����������ȡ��һ������
				Socket client = serverSocket.accept();
				// �����������
				new HandlerThread(client);
			}
		} catch (Exception e) {
			System.out.println("�������쳣: " + e.getMessage());
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
				// ��ȡ�ͻ�������
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientInputStr = input.readLine();// ����Ҫע��Ϳͻ����������д������Ӧ,������� EOFException
				// ����ͻ�������
				System.out.println("�ͻ��˷�����������:" + clientInputStr);

				// ��ͻ��˻ظ���Ϣ
				PrintStream out = new PrintStream(socket.getOutputStream());

				out.println("helloBack");

				out.close();
				input.close();
			} catch (Exception e) {
				System.out.println("������ run �쳣: " + e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						socket = null;
						System.out.println("����� finally �쳣:" + e.getMessage());
					}
				}
			}
		}
	}
}
