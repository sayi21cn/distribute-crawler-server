package xu.main.java.distribute_crawler_server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import xu.main.java.distribute_crawler_server.config.NetConfig;

public class TaskSpeedUdpClient {

	byte[] buff = new byte[NetConfig.UDP_DATA_LEN];

	private DatagramPacket outPacket = null;

	public void init() {

		try {
			DatagramSocket socket = new DatagramSocket();
			outPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(NetConfig.INET_SOCKET_ADDRESS), NetConfig.UDP_TASK_RECORD_SERVER_PORT);
			while (true) {
				System.out.println("send data:\t{\"taskid\":\"1\"}");
				outPacket.setData("{\"taskId\":\"1\"}".getBytes());
				socket.send(outPacket);
				Thread.sleep(100);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		TaskSpeedUdpClient taskSpeedUdpClient = new TaskSpeedUdpClient();
		taskSpeedUdpClient.init();
	}

}
