package xu.main.java.distribute_crawler_server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_common.conn_data.ExtractResultVO;
import xu.main.java.distribute_crawler_common.util.GsonUtil;
import xu.main.java.distribute_crawler_common.util.StringHandler;
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

	public void insertResultToDb(ExtractResultVO extractResultVO) {
		PreparedStatement pstmt = null;
		String insertTableName = extractResultVO.getSaveTableName();

		try {
			Connection conn = MysqlUtil.getConnection();

			Map<String, String> resultMap = extractResultVO.getResult();
			if (null == resultMap) {
				throw new RuntimeException("ResultMap is NULL in ExtractResultVO at DbDao.java 46");
			}
			if (resultMap.isEmpty()) {
				throw new RuntimeException("ResultMap is Empty in ExtractResultVO at DbDao.java 46");
			}

			List<String> valueList = new ArrayList<String>();
			String insertSql = buildSaveSQL(insertTableName, resultMap, valueList);

			logger.debug(String.format("insert Sql : %s", insertSql));
			pstmt = conn.prepareStatement(insertSql);

			for (int valueIndex = 0, len = valueList.size(); valueIndex < len; valueIndex++) {
				pstmt.setString(valueIndex + 1, valueList.get(valueIndex));
			}

			pstmt.execute();

		} catch (ClassNotFoundException e) {
			logger.error("Class not found Exception", e);
		} catch (SQLException e) {
			logger.error("SQLException", e);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			MysqlUtil.closePreparedStatement(pstmt);
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

	public String buildeTableExistSql(String tableName) {

		return "";
	}

	public String buildSaveSQL(String insertTableName, Map<String, String> resultMap, List<String> valueList) {
		StringBuilder insertSqlBuilder = new StringBuilder("insert into ");
		insertSqlBuilder.append(insertTableName).append(" (");
		StringBuilder valueBuilder = new StringBuilder(" (");

		for (Iterator<Entry<String, String>> it = resultMap.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			String column = entry.getKey();
			String value = entry.getValue();

			if (StringHandler.isNullOrEmpty(column)) {
				String taskId = resultMap.get("task_id");
				logger.warn(String.format("DbDao: Task [%s] return a NULL column , result json : [ %s ]", taskId, GsonUtil.toJson(resultMap)));
				continue;
			}
			if (StringHandler.isNullOrEmpty(value)) {
				String taskId = resultMap.get("task_id");
				logger.warn(String.format("DbDao: Task [%s] return a NULL value , result json : [ %s ]", taskId, GsonUtil.toJson(resultMap)));
				continue;
			}

			insertSqlBuilder.append(column).append(" ,");
			valueBuilder.append("?,");
			valueList.add(value);

		}
		deleteBuilderLast(insertSqlBuilder, 1);
		insertSqlBuilder.append(") values");
		deleteBuilderLast(valueBuilder, 1);
		insertSqlBuilder.append(valueBuilder.toString());
		insertSqlBuilder.append(");");
		return insertSqlBuilder.toString();
	}

	private void deleteBuilderLast(StringBuilder builder, int deleteNum) {
		if (null == builder || builder.length() <= deleteNum) {
			return;
		}
		if (deleteNum == 1) {
			builder.deleteCharAt(builder.length() - 1);
			return;
		}
		builder.delete(builder.length() - deleteNum - 1, builder.length() - 1);
	}

	public static void main(String[] args) {
		DbDao dbDao = new DbDao();

		String insertTableName = "test_name";
		Map<String, String> resultMap = new HashMap<String, String>();
		List<String> valueList = new ArrayList<String>();
		resultMap.put("download_url", "http://www.www.com");
		resultMap.put("errorMessage", "none");

		System.out.println(dbDao.buildSaveSQL(insertTableName, resultMap, valueList));
	}

}
