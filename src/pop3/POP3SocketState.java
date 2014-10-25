package pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class POP3SocketState {

    public final SocketChannel clientChannel;
    public SocketChannel pop3ServerChannel;
    
    private boolean serverConnected;
    private POP3ProtocolState pop3State;
    private POP3Command lastCommand;
    
    POP3SocketState(final SocketChannel clientChannel) {
	
	serverConnected = false;
	pop3State = POP3ProtocolState.AUTHORIZATION;
	lastCommand = null;
	
        if (clientChannel == null) {
            throw new IllegalArgumentException();
        }
        
        this.clientChannel = clientChannel;
    }
    
    public boolean hasServerConnected() {
	return serverConnected;
    }
    
    public POP3ProtocolState getPOP3State() {
	return pop3State;
    }
    
}
