package xu.main.java.distribute_crawler_server.db;

import java.util.List;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.vo.TaskRecord;
import xu.main.java.distribute_crawler_server.config.ServerDbConfig;
import xu.main.java.distribute_crawler_server.queue.JobCenter;

public class DbTracker extends Thread {

	private Logger logger = Logger.getLogger(DbTracker.class);

	private DbDao dbDao = new DbDao();

	@Override
	public void run() {
		logger.info("DbTracker: start !");
		while (true) {

			List<TaskRecord> taskRecordList = dbDao.queryAllWaitTaskRecords();

			// TODO:更新数据库状态，避免下次再被加入到任务队列中

			logger.info(String.format("DbTracker:query db finished,task size : " + taskRecordList.size()));

			for (int taskRecordIndex = 0, len = taskRecordList.size(); taskRecordIndex < len; taskRecordIndex++) {
				TaskRecord taskRecord = taskRecordList.get(taskRecordIndex);
				logger.info(String.format("DbTracker:Query task id [%s],task_name [%s], add to waitTaskRecordQueue", taskRecord.getId(), taskRecord.getTask_name()));
				JobCenter.offerTaskRecordToWaitQueue(taskRecord);
			}
			logger.debug("Task Wait Queue Size : " + JobCenter.taskWaitQueueSize());

			try {
				logger.info(String.format("Query Task Finished, sleep [ %s ]ms", ServerDbConfig.DB_TRACKER_QUERY_INTERVAL));
				Thread.sleep(ServerDbConfig.DB_TRACKER_QUERY_INTERVAL);
			} catch (InterruptedException e) {
				logger.error("DbTracker sleep error ! ", e);
			}

		}
	}

}
