package proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class POP3SocketHandler implements TCPProtocol {

    private Set<SocketChannel> newClients;
    
    public POP3SocketHandler() {
	
	newClients = new HashSet<SocketChannel>();
	
    }
    
    @Override
    public void handleAccept(SelectionKey key) throws IOException {
	
	ServerSocketChannel listenChannel = (ServerSocketChannel)key.channel();
	
	System.out.println("Handle Accept");
	
	SocketChannel clientChannel = listenChannel.accept();
	clientChannel.configureBlocking(false);
	newClients.add(clientChannel);
	NewSocketState state = new NewSocketState(clientChannel);
	
	clientChannel.register(key.selector(), SelectionKey.OP_READ, state);
	
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {
	
	SocketChannel readChannel = (SocketChannel)key.channel();
	
	if (newClients.contains(readChannel)) {
	    
	    System.out.println("client is new");
	    NewSocketState state = (NewSocketState)key.attachment();
	    state.handleRead(key.selector());
	   
	} else {
	    
	}
	
	
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void handleConnect(SelectionKey key) throws IOException {
	// TODO Auto-generated method stub
	
    }

}
