package pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import proxy.ServerConfig;
import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {

    private POP3CommandParser pop3Parser;
    private ServerConfig serverConfig;

    public POP3SocketHandler(ServerConfig serverConfig) throws IOException {
	
	pop3Parser = new POP3CommandParser("resources/pop3.properties");
	this.serverConfig = serverConfig;
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {

	ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();

	System.out.println("Handle Accept");

	SocketChannel clientChannel = listenChannel.accept();
	clientChannel.configureBlocking(false);
	
	POP3SocketState socketState = new POP3SocketState(clientChannel);
	clientChannel.register(key.selector(), SelectionKey.OP_WRITE, socketState);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {

	SocketChannel readChannel = (SocketChannel) key.channel();

    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

	SocketChannel writeChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();
	
	POP3Command lastCommand = state.getLastPOP3Command();
	
	switch(state.getPOP3State()) {
	case AUTHORIZATION:
	    
	    /* Enviar greeting message */
	    if (lastCommand == null) {
		
		ByteBuffer buf = state.writeBufferFor(writeChannel);
		buf.put(pop3Parser.getCommandString(POP3Command.OK).getBytes());
		buf.put("\r\n".getBytes());
		writeChannel.write(buf);
		key.cancel();
	    }
	    
	    break;
	    
	case TRANSACTION:
	    break;
	    
	case UPDATE:
	    break;
	}

    }

    @Override
    public void handleConnect(SelectionKey key) throws IOException {

    }

}
