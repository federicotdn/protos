package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NewSocketState {
    
    private ByteBuffer outBuffer;
    private ByteBuffer inBuffer;
    private SocketChannel channel;
    
    private String greeting = "+OK POP3 server ready";
    
    private static int BUF_SIZE = 1024;
    
    public NewSocketState(SocketChannel channel) {
	if (channel == null) {
	    throw new IllegalArgumentException();
	}
	
	this.channel = channel;
	inBuffer = ByteBuffer.allocate(BUF_SIZE);
	outBuffer = ByteBuffer.allocate(BUF_SIZE);
    }
    
    public void handleWrite(Selector selector) {
	
    }
    
    public void handleRead(Selector selector) throws IOException {
	
	//todavia no estamos conectados a ningun servidor POP3
	//TODO: manejar CAPA
	channel.read(inBuffer);
	byte[] bytes = inBuffer.array();
	
	String[] msg = new String(bytes).split("\\s+");
	for (String part : msg) {
	    
	    System.out.println(part);
	    
	}
    }
    
}
