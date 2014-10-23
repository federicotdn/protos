package proxy;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import pop3.POP3SocketHandler;
import rcp.RCPSocketHandler;

public class ProxyServer {

	private ServerConfig config;

	private Selector selector;

	private ServerSocketChannel pop3ListenChannel;
	private ServerSocketChannel rcpListenChannel;

	private POP3SocketHandler pop3Handler;
	private RCPSocketHandler rcpHandler;

	HashMap<SocketChannel, TCPProtocol> socketHandlers;

	public ProxyServer() {

		config = new ServerConfig();
		socketHandlers = new HashMap<SocketChannel, TCPProtocol>();
		
	}

	public void init() throws IOException {

		selector = Selector.open();

		pop3ListenChannel = ServerSocketChannel.open();
		pop3ListenChannel.socket().bind(config.getPop3Address());
		pop3ListenChannel.configureBlocking(false);
		
		rcpListenChannel = ServerSocketChannel.open();
		rcpListenChannel.socket().bind(config.getRcpAddress());
		rcpListenChannel.configureBlocking(false);
	}

	public void begin() throws IOException {

		pop3ListenChannel.register(selector, SelectionKey.OP_ACCEPT);
		rcpListenChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {

			selector.select();

			Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				
				TCPProtocol handler;
				SelectionKey key = keyIterator.next();

				if (key.isValid() && key.isAcceptable()) {

					ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();
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
						handler = getSocketHandler(key);
						handler.handleRead(key);
					}
	
					if (key.isValid() && key.isWritable()) {
						handler = getSocketHandler(key);
						handler.handleWrite(key);
					}
	
					if (key.isValid() && key.isConnectable()) {
						handler = getSocketHandler(key);
						handler.handleConnect(key);
					}
				
				} catch (Exception e) { // cambiar tipo de excepcion
					
				}

				keyIterator.remove();
			}

		}

	}
	
	private TCPProtocol getSocketHandler(SelectionKey key) throws Exception { //cambiar tipo de excepcion
		SelectableChannel channel = key.channel();
		TCPProtocol handler = socketHandlers.get(channel);
		
		if (handler == null) {
			throw new Exception();
		}
		
		return handler;
	}
}