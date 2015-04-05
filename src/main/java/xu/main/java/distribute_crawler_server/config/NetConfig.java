package xu.main.java.distribute_crawler_server.config;

public class NetConfig {

	public static final String INET_SOCKET_ADDRESS = "192.168.1.10";

	/* Task push nio server */

	public static final int NIO_TASK_QUERY_SERVER_PORT = 5667;

	public static final String NIO_CHARSET = "UTF-8";

	public static final int NIO_PUSH_TASK_INTERVEL = 1000 * 30;

	/* Task Speed Feedback Udp Server */

	public static final int UDP_TASK_RECORD_SERVER_PORT = 5668;

	public static final int UDP_DATA_LEN = 2048;

}
