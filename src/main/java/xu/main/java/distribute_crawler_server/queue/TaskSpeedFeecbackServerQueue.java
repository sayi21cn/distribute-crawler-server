package xu.main.java.distribute_crawler_server.queue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class TaskSpeedFeecbackServerQueue {

	private static Queue<String> speedQueue = new LinkedBlockingDeque<String>();

	public static boolean offerFeedback(String feedbackJson) {

		return speedQueue.offer(feedbackJson);
	}

	public static String peekFeedbcak() {
		return speedQueue.peek();
	}

}
