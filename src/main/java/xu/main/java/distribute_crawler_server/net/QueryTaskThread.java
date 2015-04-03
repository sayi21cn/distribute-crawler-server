package xu.main.java.distribute_crawler_server.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_server.job.JobTracker;

public class QueryTaskThread extends Thread {

	private Logger logger = Logger.getLogger(QueryTaskThread.class);
	private Socket socket;
	private JobTracker jobTracker;

	public QueryTaskThread(Socket socket, JobTracker jobTracker) {
		this.jobTracker = jobTracker;
	}

	@Override
	public void run() {
		InputStream inputStream = null;
		try {
			inputStream = this.socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeInputStrem(inputStream);
			closeSocket(this.socket);
		}

	}

	private void closeInputStrem(InputStream inputStream) {
		if (null != inputStream) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	private void closeSocket(Socket socket) {
		if (null != socket && !socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

}
