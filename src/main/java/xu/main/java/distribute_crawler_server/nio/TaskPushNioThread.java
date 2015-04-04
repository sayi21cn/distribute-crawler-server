package xu.main.java.distribute_crawler_server.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.nio_data.TaskVO;
import xu.main.java.distribute_crawler_common.util.GsonUtil;
import xu.main.java.distribute_crawler_common.util.StringHandler;
import xu.main.java.distribute_crawler_common.vo.TaskRecord;
import xu.main.java.distribute_crawler_common.vo.TemplateContentVO;
import xu.main.java.distribute_crawler_server.config.NioServerConfig;
import xu.main.java.distribute_crawler_server.config.ServerDbConfig;
import xu.main.java.distribute_crawler_server.db.DbDao;
import xu.main.java.distribute_crawler_server.job.JobCenter;

public class TaskPushNioThread extends Thread {

	private Map<String, SocketChannel> socketMap;

	private Charset charset = Charset.forName(NioServerConfig.NIO_CHARSET);

	private Logger logger = Logger.getLogger(TaskPushNioThread.class);

	private DbDao dbDao = new DbDao();

	public TaskPushNioThread(Map<String, SocketChannel> socketMap) {

		this.socketMap = socketMap;
	}

	@Override
	public void run() {
		try {
			push();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void push() throws IOException {

		while (true) {

			if (socketMap.size() < 1) {
				logger.warn(String.format("无客户端连接.sleep [%s]ms",NioServerConfig.POLL_TASK_INTERVEL));
				threadSleep();
				continue;
			}

			TaskVO taskVO = new TaskVO();
			TaskRecord taskRecord = JobCenter.pollWaitTaskRecord();
			if (null == taskRecord) {
				logger.warn(String.format("任务队列无任务,sleep [%s]ms",NioServerConfig.POLL_TASK_INTERVEL));
				threadSleep();
				continue;
			}
			String templateArea = queryTemplateById(taskRecord.getTemplate_id());
			try {
				TemplateContentVO templateContentVO = GsonUtil.fromJson(templateArea, TemplateContentVO.class);
				taskVO.setTemplateContentVO(templateContentVO);
			} catch (Exception e) {
				logger.error("Content Extractor model extracted error!", e);
				continue;
			}
			taskVO.setTaskId(taskRecord.getId());
			taskVO.setTaskName(taskRecord.getTask_name());
			taskVO.setInsertDbTableName(taskRecord.getInsert_db_table_name());
			taskVO.setThreadNum(StringHandler.string2Int(taskRecord.getDownload_thread_num(), 1));

			Queue<String> urlQueue = this.extractUrls(taskRecord);
			taskVO.setUrlQueue(urlQueue);
			taskVO.setUrlCount(urlQueue.size());
			boolean isPushed = pushTaskVO(taskVO);

			logger.info("任务下发 " + (isPushed == true ? "成功" : "失败"));

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (isPushed) {
				continue;
			}

			JobCenter.offerTaskRecordToWaitQueue(taskRecord);
		}
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

	public boolean pushTaskVO(TaskVO taskVO) {
		SocketChannel sc = randomSocketChannel();
		if (null == sc) {
			logger.warn("任务下发失败，无可推送的客户端。");
			return false;
		}
		String remoteAddress = "";
		try {
			remoteAddress = sc.getRemoteAddress().toString();
			String taskJson = GsonUtil.toJson(taskVO);
			logger.info(String.format("向[ %s ]下发任务，任务id:[%s]", sc.getRemoteAddress(), taskVO.getTaskId()));
			logger.debug("下发内容: " + taskJson);
			sc.write(charset.encode(taskJson));
		} catch (IOException e) {
			socketMap.remove(remoteAddress);
			return pushTaskVO(taskVO);
		}

		return true;
	}

	public SocketChannel randomSocketChannel() {

		SocketChannel socketChannel = null;
		while (true) {
			int size = socketMap.keySet().size();
			if (size < 1) {
				break;
			}
			Random random = new Random();
			int randomIndex = random.nextInt(size);
			String socketAddress = (String) socketMap.keySet().toArray()[randomIndex];
			socketChannel = socketMap.get(socketAddress);

			if (socketChannel.isConnected()) {
				break;
			}
			socketMap.remove(socketAddress);
			logger.info("移除客户端: " + socketAddress);
		}
		return socketChannel;
	}

	public String queryTemplateById(String templateId) {
		return dbDao.queryTemplateById(templateId);
	}

	public void threadSleep() {
		try {
			Thread.sleep(NioServerConfig.POLL_TASK_INTERVEL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
