package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NewSocketState {
    
    private ByteBuffer buffer;
    private SocketChannel channel;
    
    private static int BUF_SIZE = 1024;
    
    public NewSocketState(SocketChannel channel) {
	if (channel == null) {
	    throw new IllegalArgumentException();
	}
	
	this.channel = channel;
	buffer = ByteBuffer.allocate(BUF_SIZE);
	
    }
    
    public void handleRead(Selector selector) throws IOException {
	
	//todavia no estamos conectados a ningun servidor POP3
	//TODO: manejar CAPA
	channel.read(buffer);
	String[] message = buffer.toString().split("\\s+");
	
    }
    
}
