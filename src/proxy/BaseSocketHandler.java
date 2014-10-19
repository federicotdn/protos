package proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class BaseSocketHandler implements TCPProtocol {
    
    private POP3ServerConfig config;
    private POP3SocketHandler pop3Handler;
    
    public BaseSocketHandler(POP3ServerConfig config) {
	
	this.config = config;
	pop3Handler = new POP3SocketHandler();
    }
    
    @Override
    public void handleAccept(final SelectionKey key) throws IOException {
	
	ServerSocketChannel listenChannel = ((ServerSocketChannel)key.channel());
	int port = listenChannel.socket().getLocalPort();
	
	if (port == POP3ProxyServer.POP3_PORT) {
	    
	    pop3Handler.handleAccept(key);
	    
	    
	} else if (port == POP3ProxyServer.ADMIN_PORT) {
	    
	} else {
	   // throw new InvalidPortException("Invalid port: " + new Integer(port).toString());
	}
	
    }
    
    @Override
    public void handleRead(SelectionKey key) throws IOException {
	
	System.out.println("Handle Read");
	SocketChannel readChannel = (SocketChannel)key.channel();
	
	int port = readChannel.socket().getLocalPort();
	
	if (port == POP3ProxyServer.POP3_PORT) {
	    
	    pop3Handler.handleRead(key);
	    
	} else if (port == POP3ProxyServer.ADMIN_PORT) {
	    
	} else {
	    //throw new InvalidValue();
	}
    }
    
    @Override
    public void handleWrite(SelectionKey key) throws IOException {
	
	System.out.println("Handle Write");
	SocketChannel writeChannel = (SocketChannel)key.channel();
	
	int port = writeChannel.socket().getLocalPort();
	
	if (port == POP3ProxyServer.POP3_PORT) {
	    
	    pop3Handler.handleWrite(key);
	    
	} else if (port == POP3ProxyServer.ADMIN_PORT) {
	    
	} else {
	    //throw new InvalidValue();
	}
    }
    
    @Override
    public void handleConnect(SelectionKey key) throws IOException {
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
