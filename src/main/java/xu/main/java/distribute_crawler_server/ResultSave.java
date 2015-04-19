package xu.main.java.distribute_crawler_server;

import java.util.Queue;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.util.TextFileWriter;
import xu.main.java.distribute_crawler_common.util.ThreadUtil;
import xu.main.java.distribute_crawler_server.config.ResultDataConfig;

public class ResultSave extends Thread {

	private Queue<String> resultQueue = null;

	private String filePath = "/opt/java/distribute-crawler/data/defaultResultFile";

	private Logger logger = Logger.getLogger(ResultSave.class);

	@Override
	public void run() {

		if (null == this.resultQueue) {
			logger.error("ResultSave Thread resultQueue NULL, return");
			return;
		}

		String result = "";
		while (true) {

			result = this.resultQueue.poll();
			if (null == result) {
				logger.info(String.format("No result in resultQueue, wait [%s]ms", ResultDataConfig.QUERY_RESULT_QUEUE_INTERVAL));
				ThreadUtil.sleep(ResultDataConfig.QUERY_RESULT_QUEUE_INTERVAL);
				continue;
			}
			TextFileWriter.write(filePath, result, true);
		}
	}

	public void setResultQueue(Queue<String> resultQueue) {
		this.resultQueue = resultQueue;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

}
