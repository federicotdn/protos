package proxy;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import config.CustomLogger;
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
	
	private static CustomLogger logger;

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
		
		logger = CustomLogger.getInsance();
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

				try {

					if (key.isValid() && key.isAcceptable()) {

						ServerSocketChannel listenChannel = (ServerSocketChannel) key
								.channel();
						int port = listenChannel.socket().getLocalPort();

						if (port == config.getPOP3Port()) {
							pop3Handler.handleAccept(key);
						} else if (port == config.getRCPPort()) {
							rcpHandler.handleAccept(key);
						} 

					}

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

				} catch (Exception e) {
					SelectableChannel channel = key.channel();
					if (channel == rcpListenChannel || channel == pop3ListenChannel) {
						for (SocketChannel c: state.getSocketHandlers().keySet()) {
							if (channel == pop3ListenChannel) {
								logger.logDisconnection(c.getRemoteAddress());
							}
							c.close();
						}
						try {
							state.saveAll();
							logger.getLogger().error("Fatal error: " + e.getCause());
						} catch (JAXBException e2) {
							logger.getLogger().error("Error: " + e.getCause());
							return;
						}
						return;
						
					} else {
						if (state.getSocketHandler(key) == pop3Handler) {
							POP3SocketState pop3State = (POP3SocketState) key.attachment();
							logger.logDisconnection("Client:", pop3State.getClientChannel().getRemoteAddress());
							state.removeSocketHandler(pop3State.getClientChannel());
							key.cancel();
							pop3State.getClientChannel().close();
							if (pop3State.getServerChannel() != null) {
								logger.logDisconnection("Server:", pop3State.getServerChannel().getRemoteAddress());
								pop3State.getServerChannel().close();
								state.removeSocketHandler(pop3State.getServerChannel());
							}
						} else  {
							key.cancel();
							state.removeSocketHandler((SocketChannel)channel);
							channel.close();	
						}
					}
				}
				keyIterator.remove();
			}

		}

	}

	
}