package proxy;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

public class ServerState {

	private HashMap<SocketChannel, TCPProtocol> socketHandlers;
	private ServerConfig config;

	public ServerState() throws JAXBException {

		socketHandlers = new HashMap<SocketChannel, TCPProtocol>();
		config = new ServerConfig();
	}

	public TCPProtocol getSocketHandler(SelectionKey key) throws Exception { // cambiar
																				// tipo
																				// de
																				// excepcion
		SelectableChannel channel = key.channel();
		TCPProtocol handler = socketHandlers.get(channel);

		if (handler == null) {
			throw new Exception();
		}

		return handler;
	}

	public void setSocketHandler(SocketChannel channel, TCPProtocol handler) {
		socketHandlers.put(channel, handler);
	}

	public ServerConfig getConfig() {
		return config;
	}
}
