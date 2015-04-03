package xu.main.java.distribute_crawler_server.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.nio_data.TaskVO;
import xu.main.java.distribute_crawler_common.util.GsonUtil;
import xu.main.java.distribute_crawler_common.vo.TaskRecord;
import xu.main.java.distribute_crawler_common.vo.TemplateContentVO;
import xu.main.java.distribute_crawler_server.config.NioServerConfig;
import xu.main.java.distribute_crawler_server.db.DbDao;
import xu.main.java.distribute_crawler_server.job.JobCenter;

public class TaskPushThread extends Thread {

	private Map<String, SocketChannel> socketMap;

	private Charset charset = Charset.forName(NioServerConfig.NIO_CHARSET);

	private Logger logger = Logger.getLogger(TaskPushThread.class);

	private DbDao dbDao = new DbDao();

	public TaskPushThread(Map<String, SocketChannel> socketMap) {

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
				logger.warn("无客户端连接...");
				threadSleep();
				continue;
			}

			TaskVO taskVO = new TaskVO();
			TaskRecord taskRecord = JobCenter.pollWaitTaskRecord();
			if (null == taskRecord) {
				logger.warn("任务队列无任务");
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

	public boolean pushTaskVO(TaskVO taskVO) {
		SocketChannel sc = randomSocketChannel();
		if (null == sc) {
			return false;
		}
		String remoteAddress = "";
		try {
			remoteAddress = sc.getRemoteAddress().toString();
			logger.info("任务下发至:" + sc.getRemoteAddress());
			sc.write(charset.encode(GsonUtil.toJson(taskVO)));
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

			System.out.println(socketChannel.isBlocking());
			System.out.println(socketChannel.isConnected());
			System.out.println(socketChannel.isConnectionPending());
			System.out.println(socketChannel.isOpen());
			System.out.println(socketChannel.isRegistered());

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
