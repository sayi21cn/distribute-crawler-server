package xu.main.java.distribute_crawler_server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;

import org.apache.log4j.Logger;

public class UdpServer extends Thread {

	private String udpServerIp = "127.0.0.1";

	private int udpServerPort = 5667;

	private int datagramPackgetMaxSize = 1024;

	private byte[] buff = null;

	private Queue<String> queue = null;

	private DatagramPacket inPacket = null;

	private Logger logger = Logger.getLogger(UdpServer.class);

	public UdpServer(String udpServerIp, int udpServerPort, int datagramPacketMaxSize) {
		this.udpServerIp = udpServerIp;
		this.udpServerPort = udpServerPort;
		this.datagramPackgetMaxSize = datagramPacketMaxSize;
		buff = new byte[datagramPackgetMaxSize];
		inPacket = new DatagramPacket(buff, datagramPackgetMaxSize);
	}

	@Override
	public void run() {

		logger.info(String.format("[ %s ] UdpServer start,bind ip:[ %s ] bind port:[ %s ]", Thread.currentThread().getName(), udpServerIp, udpServerPort));

		try {
			DatagramSocket socket = new DatagramSocket(udpServerPort, InetAddress.getByName(udpServerIp));
			while (true) {
				socket.receive(inPacket);
				String feedbackJson = new String(buff, 0, inPacket.getLength());

				logger.info("receive:\t" + feedbackJson);
				if (null != this.queue) {
					this.queue.offer(feedbackJson);
				}
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

	public void setQueue(Queue<String> queue) {
		this.queue = queue;
	}

	public static void main(String[] args) {
		UdpServer taskSpeedUdpServer = new UdpServer("", 5667, 1024);
		taskSpeedUdpServer.run();
	}

}
