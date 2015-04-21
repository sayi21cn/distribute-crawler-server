package xu.main.java.distribute_crawler_server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import xu.main.java.distribute_crawler_server.config.ServerDbConfig;

public class MysqlUtil {

	private static Connection conn = null;

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException, ClassNotFoundException {

		if (null != conn && !conn.isClosed()) {
			return conn;
		}

		String mysqlUrl = "jdbc:mysql://" + ServerDbConfig.SERVER_IP + ":" + ServerDbConfig.SERVER_PORT + "/" + ServerDbConfig.DB_NAME + "?useUnicode=true&characterEncoding=utf-8";
		conn = DriverManager.getConnection(mysqlUrl, ServerDbConfig.DB_USER_NAME, ServerDbConfig.DB_PASS_WORD);
		return conn;
	}

	public static boolean saveToDb(Connection conn, String sql) {
		Statement stat = null;
		boolean result = true;
		try {
			stat = conn.createStatement();
			stat.execute(sql);
		} catch (SQLException e) {
			result = false;
			e.printStackTrace();
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static void closeStatement(Statement statement) {
		if (null != statement) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void closePreparedStatement(PreparedStatement pstmt) {
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
