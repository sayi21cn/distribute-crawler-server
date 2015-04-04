package xu.main.java.distribute_crawler_server;

import org.apache.log4j.PropertyConfigurator;

import xu.main.java.distribute_crawler_server.db.DbTracker;
import xu.main.java.distribute_crawler_server.nio.TaskPushNioServer;

public class CrawlerServerStart {

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");

		// 数据库线程启动
		DbTracker dbTracker = new DbTracker();
		dbTracker.start();

		// JobTracker线程启动
		// JobTracker jobTacker = new JobTracker();
		// jobTacker.start();

		// Task Push NIO Server 线程启动
		TaskPushNioServer taskPushNioServer = new TaskPushNioServer();
		taskPushNioServer.start();

	}

}
