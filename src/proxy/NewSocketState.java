package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import pop3.NewClientStatus;

public class NewSocketState {

    private ByteBuffer outBuffer;
    private ByteBuffer inBuffer;
    private SocketChannel channel;
    private NewClientStatus status;

    private String greeting = "+OK POP3 server ready";

    private static int BUF_SIZE = 1024;

    public NewSocketState(SocketChannel channel) {
	if (channel == null) {
	    throw new IllegalArgumentException();
	}

	this.channel = channel;
	inBuffer = ByteBuffer.allocate(BUF_SIZE);
	outBuffer = ByteBuffer.allocate(BUF_SIZE);
	
	status = NewClientStatus.NEW;
    }

    public void handleWrite(SelectionKey key) throws IOException {
	
	//todavia no estamos conectados a ningun servidor POP3
	switch (status) {
	
	case NEW:
	    System.out.println("write to pop3 client");
	    outBuffer.clear();
	    outBuffer.put(greeting.getBytes());
	    
	    channel.write(outBuffer);
	    
	    channel.register(key.selector(), SelectionKey.OP_READ, this);
	    
	    break;
	case ERROR:
	    
	    
	    
	    break;
	    
	}
	
    }

    public void handleRead(SelectionKey key) throws IOException {

	// todavia no estamos conectados a ningun servidor POP3
	// TODO: manejar CAPA
	channel.read(inBuffer);
	byte[] bytes = inBuffer.array();

	String[] msg = new String(bytes).split("\\s+");
	for (String part : msg) {

	    System.out.println(part);

	}
	
	inBuffer.clear();
	key.cancel();
	
    }

}
