package proxy;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import config.XMLManager;

public class ServerState {

    private HashMap<SocketChannel, TCPProtocol> socketHandlers;
    private ServerConfig config;
    private ServerStatistics stats;

    public ServerState() throws JAXBException, IOException {

	socketHandlers = new HashMap<SocketChannel, TCPProtocol>();
	config = new ServerConfig();
	stats = XMLManager.getInstance().loadServerStatistics();
    }

    public TCPProtocol getSocketHandler(SelectionKey key) throws Exception {
	// TODO: Cambiar tipo de excepcion

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

    public void removeSocketHandler(SocketChannel channel) {
	if (channel != null) {
	    socketHandlers.remove(channel);
	}

    }

    public HashMap<SocketChannel, TCPProtocol> getSocketHandlers() {
	return socketHandlers;
    }

    public String getUserPOP3Server(String user) {

	Map<String, String> userMap = config.getUsers();

	if (config.isMultiplexingEnabled() && userMap.containsKey(user)) {
	    return userMap.get(user);
	} else {
	    return config.getDefaultPOP3Server();
	}
    }

    public ServerConfig getConfig() {
	return config;
    }

    public ServerStatistics getStats() {
	return stats;
    }

    public void saveAll() throws JAXBException {
	config.saveParams();
	config.saveTransformations();
	config.saveUsers();
	stats.save();
    }

}
