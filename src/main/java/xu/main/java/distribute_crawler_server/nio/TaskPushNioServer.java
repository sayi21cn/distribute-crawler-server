package xu.main.java.distribute_crawler_server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import xu.main.java.distribute_crawler_server.config.NioServerConfig;

public class TaskPushNioServer extends Thread {

	private Selector selector;

//	private Charset charset = Charset.forName(NioServerConfig.NIO_CHARSET);

	private Map<String, SocketChannel> socketMap = new HashMap<String, SocketChannel>();
	
	private Logger logger = Logger.getLogger(TaskPushNioThread.class);

	@Override
	public void run() {
		
		try {
			
			TaskPushNioThread taskPushThread = new TaskPushNioThread(socketMap);
			
			taskPushThread.start();
			
			acceptCollection();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void acceptCollection() throws IOException {

		selector = Selector.open();

		ServerSocketChannel server = ServerSocketChannel.open();

		InetSocketAddress inetSocketAddress = new InetSocketAddress(NioServerConfig.INET_SOCKET_ADDRESS, NioServerConfig.TASK_QUERY_NIO_SERVER_PORT);

		server.bind(inetSocketAddress);

		server.configureBlocking(false);

		server.register(selector, SelectionKey.OP_ACCEPT);

		while (selector.select() > 0) {

			for (SelectionKey sk : selector.selectedKeys()) {

				selector.selectedKeys().remove(sk);

				System.out.print("获取连接 : ");

				if (sk.isAcceptable()) {
					
					SocketChannel sc = server.accept();

					logger.info(sc.getRemoteAddress() + " connected .");

					socketMap.put(sc.getRemoteAddress().toString(), sc);

				//	sc.write(charset.encode("server received connection ."));
				}

				sk.interestOps(SelectionKey.OP_ACCEPT);
				
				
				
				
			}

		}
	}
}
