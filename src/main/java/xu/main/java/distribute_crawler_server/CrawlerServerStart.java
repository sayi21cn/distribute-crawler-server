package xu.main.java.distribute_crawler_server;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.PropertyConfigurator;

import xu.main.java.distribute_crawler_server.config.NetConfig;
import xu.main.java.distribute_crawler_server.db.DbTracker;
import xu.main.java.distribute_crawler_server.nio.TaskPushNioServer;
import xu.main.java.distribute_crawler_server.udp.UdpServer;

public class CrawlerServerStart {

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");

		/* 任务推送队列 */
		Queue<String> pushQueue = new LinkedBlockingDeque<String>();

		/* 进度反馈收集队列 */
		Queue<String> speedQueue = new LinkedBlockingDeque<String>();

		/* 数据抽取结果收集队列 */
		Queue<String> resultQueue = new LinkedBlockingDeque<String>();

		// 数据库任务查询线程启动
		DbTracker dbTracker = new DbTracker();
		dbTracker.setTaskQueue(pushQueue);
		dbTracker.start();

		// Task Push NIO Server 线程启动
		TaskPushNioServer taskPushNioServer = new TaskPushNioServer();
		taskPushNioServer.setTaskQueue(pushQueue);
		taskPushNioServer.start();

		// Task Speed Feedback UDP Server线程启动
		UdpServer speedUdpServer = new UdpServer(NetConfig.INET_SOCKET_ADDRESS, NetConfig.UDP_TASK_SPEED_FEEDBACK_SERVER_PORT, NetConfig.UDP_SPEED_DATA_LEN);
		speedUdpServer.setQueue(speedQueue);
		speedUdpServer.setName("TaskSpeedFeedbackUdpServer");
		speedUdpServer.start();

		// Task Extract Result UDP Server线程启动
		UdpServer extractResultUdpServer = new UdpServer(NetConfig.INET_SOCKET_ADDRESS, NetConfig.UDP_EXTRACT_RESULT_SERVER_PORT, NetConfig.UDP_EXTRACT_RESULT_DATA_LEN);
		extractResultUdpServer.setQueue(resultQueue);
		extractResultUdpServer.setName("TaskExtractResulUdpServer");
		extractResultUdpServer.start();

	}

}
