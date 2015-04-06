package xu.main.java.distribute_crawler_server;

import xu.main.java.distribute_crawler_server.config.NetConfig;
import xu.main.java.distribute_crawler_server.db.DbTracker;
import xu.main.java.distribute_crawler_server.nio.TaskPushNioServer;
import xu.main.java.distribute_crawler_server.udp.UdpServer;

public class CrawlerServerStart {

	public static void main(String[] args) {

		// PropertyConfigurator.configure("log4j.properties");

		// 数据库线程启动
		DbTracker dbTracker = new DbTracker();
		dbTracker.start();

		// Task Push NIO Server 线程启动
		TaskPushNioServer taskPushNioServer = new TaskPushNioServer();
		taskPushNioServer.start();

		// Task Speed Feedback UDP Server线程启动
		UdpServer speedUdpServer = new UdpServer(NetConfig.INET_SOCKET_ADDRESS, NetConfig.UDP_TASK_SPEED_FEEDBACK_SERVER_PORT, NetConfig.UDP_SPEED_DATA_LEN);
		speedUdpServer.setName("TaskSpeedFeedbackUdpServer");
		speedUdpServer.start();

		// Task Extract Result UDP Server线程启动
		UdpServer extractResultUdpServer = new UdpServer(NetConfig.INET_SOCKET_ADDRESS, NetConfig.UDP_EXTRACT_RESULT_SERVER_PORT, NetConfig.UDP_EXTRACT_RESULT_DATA_LEN);
		extractResultUdpServer.setName("TaskExtractResulUdpServer");
		extractResultUdpServer.start();

	}

}
