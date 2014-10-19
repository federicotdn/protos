package proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.omg.CORBA.DynAnyPackage.InvalidValue;

public class SocketHandler {
    
    private POP3ServerConfig config;
    private Set<SocketChannel> newClients;
    
    public SocketHandler(POP3ServerConfig config) {
	
	this.config = config;
	
	newClients = new HashSet<SocketChannel>();
    }
    
    void handleAccept(final SelectionKey key) throws IOException, InvalidValue {
	
	ServerSocketChannel listenChannel = ((ServerSocketChannel)key.channel());
	int port = listenChannel.socket().getLocalPort();
	
	if (port == POP3ProxyServer.POP3_PORT) {
	    
	    popHandleAccept(key, listenChannel);
	    
	    
	} else if (port == POP3ProxyServer.ADMIN_PORT) {
	    
	} else {
	    throw new InvalidValue();
	}
	
    }
    
    void popHandleAccept(final SelectionKey key, ServerSocketChannel listenChannel) throws IOException {
	
	System.out.println("Handle Accept");
	
	SocketChannel clientChannel = listenChannel.accept();
	clientChannel.configureBlocking(false);
	newClients.add(clientChannel);
	NewSocketState state = new NewSocketState(clientChannel);
	
	clientChannel.register(key.selector(), SelectionKey.OP_READ, state);
    }
    
    void handleRead(SelectionKey key) throws IOException, InvalidValue {
	
	System.out.println("Handle Read");
	SocketChannel readChannel = (SocketChannel)key.channel();
	
	int port = readChannel.socket().getLocalPort();
	
	if (port == POP3ProxyServer.POP3_PORT) {
	    
	    popHandleRead(key, readChannel);
	    
	} else if (port == POP3ProxyServer.ADMIN_PORT) {
	    
	} else {
	    throw new InvalidValue();
	}
    }
    
    void popHandleRead(SelectionKey key, SocketChannel readChannel) throws IOException {
	
	if (newClients.contains(readChannel)) {
	    
	    System.out.println("client is new");
	    NewSocketState state = (NewSocketState)key.attachment();
	    state.handleRead(key.selector());
	   
	} else {
	    
	}
	
	
    }
    
    void handleWrite(SelectionKey key) throws IOException {
	
	System.out.println("Handle Write");
	
	SocketChannel clientChannel = (SocketChannel)key.channel();
	
	ByteBuffer buf = ByteBuffer.allocate(100);
	buf.put("chupala javier".getBytes());
	
	clientChannel.write(buf);
	
	clientChannel.register(key.selector(), SelectionKey.OP_READ);
    }
    
    void handleConnect(SelectionKey key) throws IOException {
	final SocketChannel clientChannel = (SocketChannel)key.attachment();
	
	SocketChannel pop3Server = (SocketChannel)key.channel();
	final POP3SocketState state = new POP3SocketState(clientChannel, pop3Server);
	
	try {
	    
	    boolean ret = pop3Server.finishConnect();
	    
	    if (ret) {
		state.updateSubscription(key.selector());
	    } else {
		state.closeChannels();
	    }
	    
	} catch (IOException e) {
	    
	    
	    
	}
	
    }
}
