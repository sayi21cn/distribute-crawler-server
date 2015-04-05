package xu.main.java.distribute_crawler_server;

import org.apache.log4j.PropertyConfigurator;

import xu.main.java.distribute_crawler_server.db.DbTracker;
import xu.main.java.distribute_crawler_server.nio.TaskPushNioServer;
import xu.main.java.distribute_crawler_server.udp.TaskSpeedUdpServer;

public class CrawlerServerStart {

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");

		// 数据库线程启动
		DbTracker dbTracker = new DbTracker();
		dbTracker.start();

		// Task Push NIO Server 线程启动
		TaskPushNioServer taskPushNioServer = new TaskPushNioServer();
		taskPushNioServer.start();
		
		// Task Speed Feedback UDP Server线程启动
		TaskSpeedUdpServer taskSpeedUdpServer = new TaskSpeedUdpServer();
		taskSpeedUdpServer.start();

	}

}
