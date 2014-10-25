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
	socketState.registerWrite(key.selector());
	
	serverState.setSocketHandler(clientChannel, this);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {

	SocketChannel readChannel = (SocketChannel) key.channel();

    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

	System.out.println("Handle write");
	
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
		buf.flip();
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
