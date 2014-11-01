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
    private String pop3ServerHostname;

    private POP3ProtocolState pop3State;
    private POP3Command lastUSERCommand;
    
    private ByteBuffer clientOutBuf;
    private ByteBuffer clientInBuf;
    
    private ByteBuffer serverOutBuf;
    private ByteBuffer serverInBuf;
    
    private StringBuffer currentLine;
    private boolean currentLineReady;
    private String lastError;

    POP3SocketState(final SocketChannel clientChannel, int bufSize) {
	
	serverConnected = false;
	pop3ServerHostname = null;
	pop3State = POP3ProtocolState.AUTHORIZATION;
	lastUSERCommand = null;
	currentLine = new StringBuffer();
	lastError = null;
	this.bufSize = bufSize;
	
        if (clientChannel == null) {
            throw new IllegalArgumentException();
        }
        
        this.clientChannel = clientChannel;
        this.pop3ServerChannel = null;
        
        clientOutBuf = ByteBuffer.allocate(this.bufSize);
        clientInBuf = ByteBuffer.allocate(this.bufSize);
        
        clientOutBuf.flip();
        clientInBuf.flip();
        
        serverOutBuf = null;
        serverInBuf = null;
    }
    
    public String getPop3ServerHostname() {
        return pop3ServerHostname;
    }

    public void setPop3ServerHostname(String pop3ServerHostname) {
        this.pop3ServerHostname = pop3ServerHostname;
    }
    
    public boolean isServerConnected() {
	return serverConnected;
    }
    
    public void setServerConnected(boolean connected) {
	serverConnected = connected;
    }
    
    public POP3ProtocolState getPOP3State() {
	return pop3State;
    }
    
    public POP3Command getLastUSERCommand() {
	return lastUSERCommand;
    }
    
    public void setLastUSERCommand(POP3Command com) {
	lastUSERCommand = com;
    }
    
    public SocketChannel getClientChannel() {
	return clientChannel;
    }
    
    public SocketChannel getServerChannel() {
	return pop3ServerChannel;
    }
    
    public void setServerChannel(SocketChannel pop3ServerChannel) {
	this.pop3ServerChannel = pop3ServerChannel;
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
    
    public void resetServerSettings() {
	setPop3ServerHostname(null);
	setServerConnected(false);
	setServerChannel(null);
    }

    public void initServerBuffers() {
	
	if (serverInBuf != null) {
	    serverInBuf.clear();
	} else {
	    serverInBuf = ByteBuffer.allocate(bufSize);
	}
	
	if (serverOutBuf != null) {
	    serverOutBuf.clear();
	} else {
	    serverOutBuf = ByteBuffer.allocate(bufSize);
	}
	
	serverInBuf.flip();
	serverOutBuf.flip();
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
    
    public void updateServerSubscription(SelectionKey key) throws ClosedChannelException {
	
	int flags = SelectionKey.OP_READ;
	
	if (serverOutBuf.hasRemaining()) {
	    flags |= SelectionKey.OP_WRITE;
	}
	
	pop3ServerChannel.register(key.selector(), flags, this);
    }
    
    public void updateClientSubscription(SelectionKey key) throws ClosedChannelException {
	
	//TODO: fijarse mejor cuando subscribirse a READ
	int flags = SelectionKey.OP_READ;
	
	if (clientOutBuf.hasRemaining()) {
	    flags |= SelectionKey.OP_WRITE;
	}
	
	clientChannel.register(key.selector(), flags, this);
    }
}
