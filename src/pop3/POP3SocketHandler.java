package pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import proxy.ServerState;
import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {

    private POP3CommandParser pop3Parser;
    private ServerState serverState;

    public POP3SocketHandler(ServerState serverState) throws IOException {
	
	pop3Parser = new POP3CommandParser("resources/pop3.properties");
	this.serverState = serverState;
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {

	ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();

	System.out.println("Handle Accept");

	SocketChannel clientChannel = listenChannel.accept();
	clientChannel.configureBlocking(false);
	
	POP3SocketState socketState = new POP3SocketState(clientChannel);
	socketState.registerClientWrite(key.selector());
	
	serverState.setSocketHandler(clientChannel, this);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {

	System.out.println("Handle Read");
	
	SocketChannel readChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();
	
	switch(state.getPOP3State()) {
	case AUTHORIZATION:
	    
	    
	    
	    break;
	    
	case TRANSACTION:
	    break;
	    
	case UPDATE:
	    break;
	}

    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

	System.out.println("Handle write");
	
	SocketChannel writeChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();
	
	POP3Command lastCommand = state.getLastPOP3Command();
	StringBuffer sb = new StringBuffer();
	
	switch(state.getPOP3State()) {
	case AUTHORIZATION:
	    
	    /* Enviar greeting message */
	    if (lastCommand == null) {
		
		ByteBuffer buf = state.writeBufferFor(writeChannel);
		sb.append(pop3Parser.getCommandString(POP3Command.OK));
		sb.append(" ").append(serverState.getConfig().getGreeting());
		
		writeChannel(writeChannel, buf, sb);
		buf.flip();
		state.registerClientRead(key.selector());
		
	    } else {
		
	    }
	    
	    break;
	    
	case TRANSACTION:
	    break;
	    
	case UPDATE:
	    break;
	}

    }
    
    private void writeChannel(SocketChannel channel, ByteBuffer buf, StringBuffer sb) throws IOException {
	sb.append("\r\n");
	byte[] bytes = sb.toString().getBytes();
	buf.put(bytes);
	channel.write(buf);
    }
    

    @Override
    public void handleConnect(SelectionKey key) throws IOException {

    }

}
