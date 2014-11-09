package proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import pop3.POP3SocketHandler;
import pop3.POP3SocketState;
import rcp.RCPSocketHandler;

public class ProxyServer {

	private ServerState state;

	private Selector selector;

	private ServerSocketChannel pop3ListenChannel;
	private ServerSocketChannel rcpListenChannel;

	private POP3SocketHandler pop3Handler;
	private RCPSocketHandler rcpHandler;

	public void init() throws IOException, JAXBException {

		state = new ServerState();

		selector = Selector.open();

		pop3ListenChannel = ServerSocketChannel.open();
		pop3ListenChannel.socket().bind(state.getConfig().getPop3Address());
		pop3ListenChannel.configureBlocking(false);

		rcpListenChannel = ServerSocketChannel.open();
		rcpListenChannel.socket().bind(state.getConfig().getRcpAddress());
		rcpListenChannel.configureBlocking(false);

		pop3Handler = new POP3SocketHandler(state);
		rcpHandler = new RCPSocketHandler(state);
	}

	public void begin() throws Exception {

		pop3ListenChannel.register(selector, SelectionKey.OP_ACCEPT);
		rcpListenChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {

			selector.select();

			Iterator<SelectionKey> keyIterator = selector.selectedKeys()
					.iterator();
			while (keyIterator.hasNext()) {

				TCPProtocol handler;
				SelectionKey key = keyIterator.next();
				ServerConfig config = state.getConfig();

				if (key.isValid() && key.isAcceptable()) {

					ServerSocketChannel listenChannel = (ServerSocketChannel) key
							.channel();
					int port = listenChannel.socket().getLocalPort();

					if (port == config.getPOP3Port()) {
						pop3Handler.handleAccept(key);
					} else if (port == config.getRCPPort()) {
						rcpHandler.handleAccept(key);
					} else {
						// throw Exception
					}

				}

				try {

					if (key.isValid() && key.isReadable()) {
						handler = state.getSocketHandler(key);
						handler.handleRead(key);
					}

					if (key.isValid() && key.isWritable()) {
						handler = state.getSocketHandler(key);
						handler.handleWrite(key);
					}

					if (key.isValid() && key.isConnectable()) {
						handler = state.getSocketHandler(key);
						handler.handleConnect(key);
					}

				} catch (Exception e) { // cambiar manejo de excepcion
					throw e;
				}

				keyIterator.remove();
			}

		}

	}
}