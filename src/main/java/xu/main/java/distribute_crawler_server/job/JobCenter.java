package xu.main.java.distribute_crawler_server.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import xu.main.java.distribute_crawler_common.vo.TaskRecord;

/**
 * 任务收集
 * 
 * @author xu
 * 
 */
// Queue
// offer 添加一个元素并返回true 如果队列已满，则返回false
// poll 移除并返问队列头部的元素 如果队列为空，则返回null
// peek 返回队列头部的元素 如果队列为空，则返回null
// put 添加一个元素 如果队列满，则阻塞
// take 移除并返回队列头部的元素 如果队列为空，则阻塞
public class JobCenter {

	private static Queue<TaskRecord> waitTaskRecordQueue = new LinkedBlockingDeque<TaskRecord>();

	/* 任务进度收集 <taskRecordId,taskRecord> */
	private static Map<Integer, TaskRecord> taskRecordMap = new HashMap<Integer, TaskRecord>();

	/* 已完成任务收集 */
	private static Queue<TaskRecord> doneTaskRecordQueue = new LinkedBlockingDeque<TaskRecord>();

	public static TaskRecord pollWaitTaskRecord() {
		return waitTaskRecordQueue.poll();
	}

	public static boolean offerTaskRecordToWaitQueue(TaskRecord taskRecord) {
		return waitTaskRecordQueue.offer(taskRecord);
	}

	public static boolean addDoneTaskRecord(TaskRecord taskRecord) {
		return doneTaskRecordQueue.offer(taskRecord);
	}

	public static TaskRecord pollDoneTaskRecord() {
		return doneTaskRecordQueue.poll();
	}

	/**
	 * 更新任务进度
	 * 
	 * @param taskRecordId
	 * @param speed
	 * @return
	 */
	public static boolean updateSpeed(int taskRecordId, String speed) {
		TaskRecord taskRecord = taskRecordMap.get(taskRecordId);
		if (null == taskRecord) {
			return false;
		}
		taskRecord.setTask_status(speed);
		return true;
	}

	/**
	 * 将TaskRecord从taskRecordMap中移除
	 * 
	 * @param taskRecord
	 * @return
	 */
	public static boolean deleteTaskRecordFromMap(TaskRecord taskRecord) {
		if (null == taskRecordMap.get(taskRecord.getId())) {
			return false;
		}
		taskRecordMap.remove(taskRecord.getId());
		return true;
	}

	public static boolean addToTaskRecordMap(TaskRecord taskRecord) {
		if (null != taskRecordMap.get(taskRecord.getId())) {
			return false;
		}
		taskRecordMap.put(taskRecord.getId(), taskRecord);
		return true;

	}

	public static void main(String[] args) {
		TaskRecord taskRecord = new TaskRecord();
		taskRecord.setId(1);
		taskRecord.setTask_status("20");

		System.out.println(taskRecord.getTask_status());

		addToTaskRecordMap(taskRecord);
		updateSpeed(1, "30");

		System.out.println(taskRecord.getTask_status());

	}
}
