package pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class POP3SocketState {
    
    private static int BUF_SIZE = 4 * 1014;

    public final SocketChannel clientChannel;
    public SocketChannel pop3ServerChannel;
    
    private boolean serverConnected;
    private POP3ProtocolState pop3State;
    private POP3Command lastCommand;
    
    private ByteBuffer clientOutBuf;
    private ByteBuffer clientInBuf;
    
    private ByteBuffer serverOutBuf;
    private ByteBuffer serverInBuf;
    
    private StringBuffer currentLine;
    private boolean currentLineReady;
    private String lastError;
    
    POP3SocketState(final SocketChannel clientChannel) {
	
	serverConnected = false;
	pop3State = POP3ProtocolState.AUTHORIZATION;
	lastCommand = null;
	currentLine = new StringBuffer();
	lastError = null;
	
        if (clientChannel == null) {
            throw new IllegalArgumentException();
        }
        
        this.clientChannel = clientChannel;
        
        clientOutBuf = ByteBuffer.allocate(BUF_SIZE);
        clientInBuf = ByteBuffer.allocate(BUF_SIZE);
    }
    
    public boolean hasServerConnected() {
	return serverConnected;
    }
    
    public POP3ProtocolState getPOP3State() {
	return pop3State;
    }
    
    public POP3Command getLastPOP3Command() {
	return lastCommand;
    }
    
    public boolean isClientSocket(SocketChannel channel) {
	return channel == clientChannel;
    }
    
    public boolean isCurrentLineReady() {
	return currentLineReady;
    }
    
    public void setCurrentLineReady(boolean ready) {
	currentLineReady = ready;
    }
    
    public String removeLastError() {
	String error = lastError;
	lastError = null;
	return error;
    }
    
    public void setLastError(String error) {
	lastError = error;
    }
    
    public boolean hasError() {
	return lastError != null;
    }
    
    public void resetCurentLine() {
	currentLine = new StringBuffer();
	setCurrentLineReady(false);
    }
    
    public StringBuffer getCurrentLine() {
	return currentLine;
    }
    
    public ByteBuffer writeBufferFor(SocketChannel channel) {

	if (clientChannel == channel) {
	    return clientOutBuf;
	} else if (pop3ServerChannel == channel) {
	    return serverOutBuf;
	} else {
	    throw new IllegalArgumentException("Buffer: socket desconocido.");
	}
    }
    
    public ByteBuffer readBufferFor(SocketChannel channel) {

	if (clientChannel == channel) {
	    return clientInBuf;
	} else if (pop3ServerChannel == channel) {
	    return serverInBuf;
	} else {
	    throw new IllegalArgumentException("Buffer: socket desconocido.");
	}
    }    
    
    public void registerClientWrite(Selector selector) throws ClosedChannelException {
	clientChannel.register(selector, SelectionKey.OP_WRITE, this);
    }
    
    public void registerClientRead(Selector selector) throws ClosedChannelException {
	clientChannel.register(selector, SelectionKey.OP_READ, this);
    }
}
