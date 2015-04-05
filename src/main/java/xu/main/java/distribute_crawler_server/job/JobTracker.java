package xu.main.java.distribute_crawler_server.job;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.conn_data.TaskVO;
import xu.main.java.distribute_crawler_common.util.GsonUtil;
import xu.main.java.distribute_crawler_common.util.StringHandler;
import xu.main.java.distribute_crawler_common.vo.TaskFeedbackVO;
import xu.main.java.distribute_crawler_common.vo.TaskRecord;
import xu.main.java.distribute_crawler_common.vo.TemplateContentVO;
import xu.main.java.distribute_crawler_server.config.ServerDbConfig;
import xu.main.java.distribute_crawler_server.db.DbDao;
import xu.main.java.distribute_crawler_server.queue.JobCenter;

/**
 * 
 * 任务分发
 * 
 * @author xu
 * 
 */

public class JobTracker extends Thread {

	private Logger logger = Logger.getLogger(JobTracker.class);
	private DbDao dbDao = new DbDao();

	@Override
	public void run() {

	}

	/**
	 * 任务进度更新
	 * 
	 * @param speedMap
	 *            <taskId,speed>
	 */
	public void taskSpeedFeedback(Map<Integer, TaskFeedbackVO> speedMap) {
		dbDao.taskSpeedFeedback(speedMap);
		dbDao.resultInsertToDb(speedMap);
	}

	public TaskVO queryTaskVO() {
		TaskVO taskVO = new TaskVO();
		TaskRecord taskRecord = JobCenter.pollWaitTaskRecord();
		if (null == taskRecord) {
			return taskVO;
		}

		String templateArea = queryTemplateById(taskRecord.getTemplate_id());
		try {
			TemplateContentVO templateContentVO = GsonUtil.fromJson(templateArea, TemplateContentVO.class);
			taskVO.setTemplateContentVO(templateContentVO);
		} catch (Exception e) {
			logger.error("Content Extractor model extracted error!", e);
			return taskVO;
		}

		taskVO.setTaskId(taskRecord.getId());
		taskVO.setTaskName(taskRecord.getTask_name());
		taskVO.setInsertDbTableName(taskRecord.getInsert_db_table_name());
		taskVO.setThreadNum(StringHandler.string2Int(taskRecord.getDownload_thread_num(), 1));

		Queue<String> urlQueue = this.extractUrls(taskRecord);
		taskVO.setUrlQueue(urlQueue);
		taskVO.setUrlCount(urlQueue.size());

		JobCenter.addToTaskRecordMap(taskRecord);
		return taskVO;
	}

	public String queryTemplateById(String templateId) {
		return dbDao.queryTemplateById(templateId);
	}

	public Queue<String> extractUrls(TaskRecord taskRecord) {
		Queue<String> queue = new LinkedBlockingDeque<String>();
		if ("2".equals(taskRecord.getIs_use_db_url())) {
			String[] urls = taskRecord.getUrls_or_sql().split(ServerDbConfig.URL_SPILT_STRING);
			int taskStatus = StringHandler.string2Int(taskRecord.getTask_status(), 0);
			for (int urlIndex = urls.length * taskStatus / 100; urlIndex < urls.length; urlIndex++) {
				queue.add(urls[urlIndex]);
			}
			return queue;
		}
		if ("1".equals(taskRecord.getIs_use_db_url())) {
			queryUrlBySql(taskRecord, queue);
			return queue;
		}
		logger.error(String.format("TaskRecord has a wrong param with is_use_db_url,id is [%s],is_use_db_url is [%s]", taskRecord.getId(), taskRecord.getIs_use_db_url()));
		return queue;
	}

	public void queryUrlBySql(TaskRecord taskRecord, Queue<String> queue) {
		String sql = taskRecord.getUrls_or_sql();
		List<String> urlList = dbDao.queryUrlBySql(sql);
		if (urlList.size() == 0) {
			return;
		}
		int taskStatus = StringHandler.string2Int(taskRecord.getTask_status(), 0);
		for (int urlIndex = urlList.size() * taskStatus / 100, len = urlList.size(); urlIndex < len; urlIndex++) {
			queue.offer(urlList.get(urlIndex));
		}
	}

}
