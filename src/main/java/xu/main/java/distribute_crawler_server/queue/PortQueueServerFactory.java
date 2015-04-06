package xu.main.java.distribute_crawler_server.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import xu.main.java.distribute_crawler_server.config.NetConfig;

/**
 * 
 * 端口号与队列对应关系工厂
 * 
 * @author xu
 * 
 */
public class PortQueueServerFactory {

	private static PortQueueServerFactory instance = null;

	private Map<Integer, Queue<String>> map = new HashMap<Integer, Queue<String>>();

	private PortQueueServerFactory() {
		init();
	}

	public static PortQueueServerFactory getInstance() {

		if (null != instance) {
			return instance;
		}

		instance = new PortQueueServerFactory();

		return instance;

	}

	public Queue<String> getQueyeByServerPort(int serverPort) {
		return map.get(serverPort);
	}

	public void putProtQueueToMap(int serverPort, Queue<String> queue) {
		map.put(serverPort, queue);
	}

	/* 进度反馈队列 */
	private Queue<String> speedQueue = new LinkedBlockingDeque<String>();

	/* 数据抽取结果队列 */
	private Queue<String> extractResultQueue = new LinkedBlockingDeque<String>();

	private void init() {
		map.put(NetConfig.UDP_TASK_SPEED_FEEDBACK_SERVER_PORT, speedQueue);
		map.put(NetConfig.UDP_EXTRACT_RESULT_SERVER_PORT, extractResultQueue);
	}

}
