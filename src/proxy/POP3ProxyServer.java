package proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import org.omg.CORBA.DynAnyPackage.InvalidValue;

public class POP3ProxyServer {
    
    public static final int POP3_PORT = 4545;
    public static final int ADMIN_PORT = 4546;
    
    private POP3ServerConfig config;
    
    private Selector selector;
    private SocketHandler socketHandler;
    
    private ServerSocketChannel popListenChannel;
    private ServerSocketChannel adminListenChannel;
    
    public POP3ProxyServer() {
	config = new POP3ServerConfig();
	
	config.setDefaultPOP3Host("localhost");
	config.setDefaultPOP3Port(POP3_PORT);
    }
    
    public void init() throws IOException {
	
	selector = Selector.open();
	socketHandler = new SocketHandler(config);
	
	popListenChannel = ServerSocketChannel.open();
	popListenChannel.socket().bind(new InetSocketAddress(POP3_PORT));
	popListenChannel.configureBlocking(false);
    }
    
    public void begin() throws IOException, InvalidValue {
	
	popListenChannel.register(selector, SelectionKey.OP_ACCEPT);
	
	while (true) {
	    
	    selector.select();
	    
	    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
	    while (keyIterator.hasNext()) {
		
		SelectionKey key = keyIterator.next();
		
		if (key.isValid() && key.isAcceptable()) {
		    socketHandler.handleAccept(key);
		}
		
		if (key.isValid() && key.isReadable()) {
		    socketHandler.handleRead(key);
		}
		
		if (key.isValid() && key.isWritable()) {
		    socketHandler.handleWrite(key);
		}
		
		if (key.isValid() && key.isConnectable()) {
		    socketHandler.handleConnect(key);
		}
		
		keyIterator.remove();
	    }
	    
	}
	
    }
}