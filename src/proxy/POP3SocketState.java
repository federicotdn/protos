package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class POP3SocketState {

    public final SocketChannel clientChannel;
    public final SocketChannel pop3ServerChannel;
    private final int BUFF_SIZE = 4 * 1024;
    
    public final ByteBuffer toPOP3ServerBuffer = ByteBuffer.allocate(BUFF_SIZE);
    public final ByteBuffer toClientBuffer = ByteBuffer.allocate(BUFF_SIZE);
    
    POP3SocketState(final SocketChannel clientChannel, final SocketChannel pop3ServerChannel) {
	
        if (clientChannel == null || pop3ServerChannel == null) {
            throw new IllegalArgumentException();
        }
        
        this.clientChannel = clientChannel;
        this.pop3ServerChannel = pop3ServerChannel;
    }

    public ByteBuffer readBufferFor(final SocketChannel channel) {
	
        final ByteBuffer ret;

        if (clientChannel == channel) {
            ret = toPOP3ServerBuffer;
        } else if (pop3ServerChannel == channel) {
            ret = toClientBuffer;
        } else {
            throw new IllegalArgumentException();
        }
            
        return ret;
    }


    public ByteBuffer writeBufferFor(SocketChannel channel) {
	
        final ByteBuffer ret;

        if (clientChannel == channel) {
            ret = toClientBuffer;
        } else if (pop3ServerChannel == channel) {
            ret = toPOP3ServerBuffer;
        } else {
            throw new IllegalArgumentException();
        }
            
        return ret;
    }
    
    public void closeChannels() throws IOException {
	
        try {
            clientChannel.close();
        } finally {
            pop3ServerChannel.close();
        }
    }

    public void updateSubscription(Selector selector) throws ClosedChannelException {
	
        int pop3ServerFlags = 0;
        int clientFlags = 0;
        
        if (toPOP3ServerBuffer.hasRemaining()) {
            clientFlags |= SelectionKey.OP_READ;
        }
        if (toClientBuffer.hasRemaining()) {
            pop3ServerFlags |= SelectionKey.OP_READ;
        }
        
        if (toPOP3ServerBuffer.position() > 0) {
            pop3ServerFlags |= SelectionKey.OP_WRITE;
        }
        if (toClientBuffer.position() > 0) {
            clientFlags |= SelectionKey.OP_WRITE;
        }
        
        
        clientChannel.register(selector, clientFlags, this);
        pop3ServerChannel.register(selector, pop3ServerFlags, this);
    }
}
