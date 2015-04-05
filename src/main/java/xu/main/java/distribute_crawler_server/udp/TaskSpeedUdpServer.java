package xu.main.java.distribute_crawler_server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_server.config.NetConfig;
import xu.main.java.distribute_crawler_server.queue.TaskSpeedFeecbackServerQueue;

public class TaskSpeedUdpServer extends Thread {

	private Logger logger = Logger.getLogger(TaskSpeedUdpServer.class);

	private byte[] buff = new byte[NetConfig.UDP_DATA_LEN];

	private DatagramPacket inPacket = new DatagramPacket(buff, NetConfig.UDP_DATA_LEN);

	@Override
	public void run() {

		logger.info(String.format("TaskSpeedUdpServer start,bind ip:[ %s ] bind port:[ %s ]", NetConfig.INET_SOCKET_ADDRESS, NetConfig.UDP_TASK_RECORD_SERVER_PORT));

		try {
			DatagramSocket socket = new DatagramSocket(NetConfig.UDP_TASK_RECORD_SERVER_PORT, InetAddress.getByName(NetConfig.INET_SOCKET_ADDRESS));
			while (true) {
				socket.receive(inPacket);
				String feedbackJson = new String(buff, 0, inPacket.getLength());
				System.out.print("receive:\t");
				System.out.println(feedbackJson);
				TaskSpeedFeecbackServerQueue.offerFeedback(feedbackJson);
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		TaskSpeedUdpServer taskSpeedUdpServer = new TaskSpeedUdpServer();
		taskSpeedUdpServer.run();
	}

}
