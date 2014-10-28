package pop3;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class POP3SocketState {
    
    private int bufSize;

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

    POP3SocketState(final SocketChannel clientChannel, int bufSize) {
	
	serverConnected = false;
	pop3State = POP3ProtocolState.AUTHORIZATION;
	lastCommand = null;
	currentLine = new StringBuffer();
	lastError = null;
	this.bufSize = bufSize;
	
        if (clientChannel == null) {
            throw new IllegalArgumentException();
        }
        
        this.clientChannel = clientChannel;
        
        clientOutBuf = ByteBuffer.allocate(this.bufSize);
        clientInBuf = ByteBuffer.allocate(this.bufSize);
        
        clientOutBuf.limit(0);
    }
    
    public boolean isServerConnected() {
	return serverConnected;
    }
    
    public POP3ProtocolState getPOP3State() {
	return pop3State;
    }
    
    public POP3Command getLastPOP3Command() {
	return lastCommand;
    }
    
    public void setLastPOP3Command(POP3Command com) {
	lastCommand = com;
    }
    
    public SocketChannel getClientChannel() {
	return clientChannel;
    }
    
    public SocketChannel getServerChannel() {
	return pop3ServerChannel;
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
    
    public void updateClientSubscription(SelectionKey key) throws ClosedChannelException {
	
	int flags = SelectionKey.OP_READ;
	
	if (clientOutBuf.hasRemaining()) {
	    flags |= SelectionKey.OP_WRITE;
	}
	
	clientChannel.register(key.selector(), flags, this);
    }
}
