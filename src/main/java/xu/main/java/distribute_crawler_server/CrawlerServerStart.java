package xu.main.java.distribute_crawler_server;

import xu.main.java.distribute_crawler_server.db.DbTracker;
import xu.main.java.distribute_crawler_server.job.JobTracker;

public class CrawlerServerStart {

	public static void main(String[] args) {

		// PropertyConfigurator.configure("etc/log4j.properties");

		// 数据库线程启动
		DbTracker dbTracker = new DbTracker();
		dbTracker.start();

		// JobTracker线程启动
		JobTracker jobTacker = new JobTracker();
		jobTacker.start();

	}

}
