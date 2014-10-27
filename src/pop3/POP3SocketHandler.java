package pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import javax.xml.bind.JAXBException;

import proxy.ServerState;
import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {

    private POP3CommandParser pop3Parser;
    private ServerState serverState;

    private static int MAX_POP3_REQ_LEN = 255;

    public POP3SocketHandler(ServerState serverState) throws IOException, JAXBException {

	pop3Parser = new POP3CommandParser("src/resources/pop3.xml");
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
	
	SocketChannel readChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();

	if (state.isClientSocket(readChannel)) {
	    handleClientRead(key, readChannel, state);
	} else {

	}
    }

    private void handleClientRead(SelectionKey key, SocketChannel clientChannel, POP3SocketState state) throws IOException {

	boolean finished = false;
	
	clientReadChannel(clientChannel, state);
	
	while (!finished) {
	    
	    clientReadLine(clientChannel, state);
	    
	    if (state.isCurrentLineReady()) {
		
		String line = state.getCurrentLine().toString();
		state.resetCurentLine();
		System.out.println(line);
		
	    } else {
		
		if (state.hasError()) {
		    
		   String error = state.removeLastError();
		   System.out.println("error: " + error);
		    
		} else {
		    
		    finished = true;
		    
		}
		
	    }
	}
	
	state.readBufferFor(clientChannel).compact();
    }

    private void clientReadLine(SocketChannel clientChannel, POP3SocketState state) {
	
	ByteBuffer buf = state.readBufferFor(clientChannel);
	Character lastChar = null;
	StringBuffer sb = state.getCurrentLine();
	
	while (buf.remaining() > 0) {

	    Character ch = (char) buf.get();
	    
	    if (isPrintableAscii(ch)) {
		
		if (sb.length() > MAX_POP3_REQ_LEN - 2) {
		    
		    //Request is too long
		    skipBufferLine(buf);
		    state.setLastError("Request was too long.");
		    break;
		    
		} else {
		    
		    sb.append(ch);
		    
		}
		
	    } else if (ch != '\r' && ch != '\n') {
		
		//Invalid ASCII character
		skipBufferLine(buf);
		state.setLastError("Invalid ASCII character received.");
		break;
		
	    } else if (lastChar == '\r' && ch == '\n') {
		
		state.setCurrentLineReady(true);
		break;
		
	    }
	    
	    lastChar = ch;
	}	
    }
    
    private void skipBufferLine(ByteBuffer buf) {
	Character lastChar = null;
	
	while (buf.hasRemaining()) {	
	    Character ch = (char) buf.get();
	    if (lastChar == '\r' && ch == '\n') {
		return;
	    }
	    lastChar = ch;
	}
    }
    
    private boolean isPrintableAscii(char ch) {
	return ch >= 32 && ch <= 126;
    }
    
    private void clientReadChannel(SocketChannel clientChannel, POP3SocketState state) throws IOException {

	ByteBuffer buf = state.readBufferFor(clientChannel);

	clientChannel.read(buf);
	buf.flip();
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

	System.out.println("Handle write");

	SocketChannel writeChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();

	POP3Command lastCommand = state.getLastPOP3Command();
	StringBuffer sb = new StringBuffer();

	switch (state.getPOP3State()) {
	case AUTHORIZATION:

	    //Enviar greeting
	    if (lastCommand == null) {

		ByteBuffer buf = state.writeBufferFor(writeChannel);
		sb.append(pop3Parser.getCommandString(POP3Command.OK));
		sb.append(" ").append(serverState.getConfig().getGreeting());

		writeChannel(writeChannel, buf, sb);
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
	buf.flip();
	channel.write(buf);
    }

    @Override
    public void handleConnect(SelectionKey key) throws IOException {

    }

}
