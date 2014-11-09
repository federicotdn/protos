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

    private POP3Line lastUSERCommand;
    
    private boolean closing;

    private ByteBuffer clientOutBuf;
    private ByteBuffer clientInBuf;
    
    private ByteBuffer serverOutBuf;
    private ByteBuffer serverInBuf;
    
    private StringBuffer currentLine;
    private boolean currentLineReady;
    private boolean currentLineInvalid;

    public POP3SocketState(final SocketChannel clientChannel, int bufSize) {
	
	serverConnected = false;
	closing = false;
	pop3ServerHostname = null;
	lastUSERCommand = null;
	currentLine = new StringBuffer();
	currentLineInvalid = false;
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
    
    public boolean isCurrentLineInvalid() {
        return currentLineInvalid;
    }

    public void setCurrentLineInvalid(boolean currentLineInvalid) {
        this.currentLineInvalid = currentLineInvalid;
    }
    
    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
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
    
    public POP3Line getLastUSERCommand() {
	return lastUSERCommand;
    }
    
    public void setLastUSERCommand(POP3Line com) {
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
    
    public void resetCurentLine() {
	currentLine = new StringBuffer();
	setCurrentLineReady(false);
    }
    
    public Character getLineLastChar() {
	int len = currentLine.length();
	
	if (len == 0) {
	    return null;
	}
	
	return currentLine.charAt(len - 1);
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
	
	int flags = 0;
	
	if (serverInBuf.limit() - serverInBuf.position() < serverInBuf.capacity()) {
	    flags |= SelectionKey.OP_READ;
	}
	
	if (serverOutBuf.hasRemaining()) {
	    flags |= SelectionKey.OP_WRITE;
	}
	
	pop3ServerChannel.register(key.selector(), flags, this);
    }
    
    public void updateClientSubscription(SelectionKey key) throws ClosedChannelException {
	
	//TODO: fijarse mejor cuando subscribirse a READ
	int flags = 0;
	
	if (clientInBuf.limit() - clientInBuf.position() < clientInBuf.capacity()) {
	    flags |= SelectionKey.OP_READ;
	}
	
	if (clientOutBuf.hasRemaining()) {
	    flags |= SelectionKey.OP_WRITE;
	}
	
	clientChannel.register(key.selector(), flags, this);
    }
}
