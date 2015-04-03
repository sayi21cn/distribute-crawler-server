package xu.main.java.distribute_crawler_server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.vo.TaskFeedbackVO;
import xu.main.java.distribute_crawler_common.vo.TaskRecord;
import xu.main.java.distribute_crawler_server.util.MysqlUtil;

/**
 * 数据库操作类
 * 
 * @author xu
 * 
 */
public class DbDao {

	private Logger logger = Logger.getLogger(DbDao.class);

	private final String QUERY_TEMPLATE_BY_ID_SQL = "select template_area from template where id = ?;";

	private final String QUERY_ALL_WAIT_TASKRECORDS = "select id,task_name,template_id,insert_db_table_name,is_use_db_url,data_category,urls_or_sql,task_describtion,task_create_date,task_update_time,download_thread_num,task_status from task where task_status = '0' order by id asc";

	private final String UPDATE_TASKRECORDS_STATUS = "update task set task_status = ? where id = ? ;";

	public void resultInsertToDb(Map<Integer, TaskFeedbackVO> speedMap){
		Statement stmt = null;
		try {
			Connection conn = MysqlUtil.getConnection();
			stmt = conn.createStatement();
			for (Iterator<Entry<Integer, TaskFeedbackVO>> it = speedMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, TaskFeedbackVO> entry = it.next();
				TaskFeedbackVO taskFeedbackVO = entry.getValue();
				List<String> sqlList = taskFeedbackVO.getInsertSqlList();
				for(int sqlIndex=0,len=sqlList.size();sqlIndex<len;sqlIndex++){
					stmt.addBatch(sqlList.get(sqlIndex));
				}
				stmt.executeBatch();
			}
			stmt.executeBatch();
		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} finally {
			MysqlUtil.closeStatement(stmt);
		}
	}
	
	public void taskSpeedFeedback(Map<Integer, TaskFeedbackVO> speedMap) {
		PreparedStatement pstmt = null;
		try {
			Connection conn = MysqlUtil.getConnection();
			pstmt = conn.prepareStatement(UPDATE_TASKRECORDS_STATUS);
			for (Iterator<Entry<Integer, TaskFeedbackVO>> it = speedMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, TaskFeedbackVO> entry = it.next();
				int taskId = entry.getKey();
				TaskFeedbackVO taskFeedbackVO = entry.getValue();
				pstmt.setString(1, String.valueOf(taskFeedbackVO.getSpeedProgress()));
				pstmt.setInt(2, taskId);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} finally {
			MysqlUtil.closePreparedStatement(pstmt);
		}
	}

	public List<TaskRecord> queryAllWaitTaskRecords() {
		List<TaskRecord> taskRecordList = new ArrayList<TaskRecord>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			Connection conn = MysqlUtil.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(QUERY_ALL_WAIT_TASKRECORDS);
			while (rs.next()) {
				TaskRecord taskRecord = new TaskRecord();
				taskRecord.setId(rs.getInt("id"));
				taskRecord.setTask_name(rs.getString("task_name"));
				taskRecord.setTemplate_id(rs.getString("template_id"));
				taskRecord.setInsert_db_table_name(rs.getString("insert_db_table_name"));
				taskRecord.setIs_use_db_url(rs.getString("is_use_db_url"));
				taskRecord.setData_category(rs.getString("data_category"));
				taskRecord.setUrls_or_sql(rs.getString("urls_or_sql"));
				taskRecord.setTask_describtion(rs.getString("task_describtion"));
				taskRecord.setTask_create_date(rs.getString("task_create_date"));
				taskRecord.setTask_update_time(rs.getString("task_update_time"));
				taskRecord.setDownload_thread_num(rs.getString("download_thread_num"));
				taskRecord.setTask_status(rs.getString("task_status"));
				taskRecordList.add(taskRecord);
			}
		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} finally {
			MysqlUtil.closeResultSet(rs);
			MysqlUtil.closeStatement(stmt);
		}

		return taskRecordList;
	}

	public String queryTemplateById(String templateId) {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String templateArea = "{}";
		try {
			Connection conn = MysqlUtil.getConnection();
			pstmt = conn.prepareStatement(QUERY_TEMPLATE_BY_ID_SQL);
			pstmt.setString(1, templateId);
			rs = pstmt.executeQuery();
			rs.next();
			templateArea = rs.getString("template_area");
		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} finally {
			MysqlUtil.closeResultSet(rs);
			MysqlUtil.closePreparedStatement(pstmt);
		}
		return templateArea;
	}

	public List<String> queryUrlBySql(String sql) {
		ResultSet rs = null;
		Statement stmt = null;
		List<String> urlList = new ArrayList<String>();
		try {
			Connection conn = MysqlUtil.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				urlList.add(rs.getString("url"));
			}
		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} finally {
			MysqlUtil.closeResultSet(rs);
			MysqlUtil.closeStatement(stmt);
		}
		return urlList;
	}

}
