package xu.main.java.distribute_crawler_server.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import xu.main.java.distribute_crawler_server.config.NioClientConfig;

public class ClientReaderThread extends Thread {

	private Selector selector;

	private Charset charset = Charset.forName(NioClientConfig.NIO_CHARSET);

	public ClientReaderThread(Selector selector) {
		this.selector = selector;
	}

	@Override
	public void run() {

		try {
			while (selector.select() > 0) {

				for (SelectionKey sk : selector.selectedKeys()) {

					selector.selectedKeys().remove(sk);

					if (sk.isReadable()) {

						SocketChannel sc = (SocketChannel) sk.channel();

						ByteBuffer buff = ByteBuffer.allocate(1024);

						String content = "";

						while (sc.read(buff) > 0) {

							buff.flip();

							content += charset.decode(buff);
						}

						System.out.println("client接收信息: " + content);

						sk.interestOps(SelectionKey.OP_READ);
					}

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
