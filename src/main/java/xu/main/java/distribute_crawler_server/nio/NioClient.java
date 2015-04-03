package xu.main.java.distribute_crawler_server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

import xu.main.java.distribute_crawler_server.config.NioClientConfig;

public class NioClient {

	private Selector selector;

	private Charset charset = Charset.forName(NioClientConfig.NIO_CHARSET);

	private SocketChannel sc = null;

	public static void main(String[] args) {

		System.out.println("Nio Client started ...");

		try {

			new NioClient().init();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public void init() throws IOException {

		selector = Selector.open();

		InetSocketAddress inetSocketAddress = new InetSocketAddress(NioClientConfig.INET_SOCKET_ADDRESS, NioClientConfig.TASK_QUERY_NIO_SERVER_PORT);

		sc = SocketChannel.open(inetSocketAddress);

		sc.configureBlocking(false);

		sc.register(selector, SelectionKey.OP_READ);

		new ClientReaderThread(selector).start();

		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			System.out.println("client 发送内容: " + line);

			sc.write(charset.encode(line));

		}

	}

}
