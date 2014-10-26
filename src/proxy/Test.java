package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.bind.JAXBException;

import exceptions.InvalidCommandException;

public class Test {

    public static void main(String[] args) throws JAXBException, InvalidCommandException {
	System.out.println("Hello, World!");
	
	ByteBuffer b = ByteBuffer.allocate(1000);
	String msg = "Hola, como te va campion";
	
	b.put(msg.getBytes());
	System.out.println(b.remaining());
	b.flip();
	System.out.println(b.remaining());
	
	byte[] bytes = new byte[b.remaining()];
	b.get(bytes);
	
	for (byte by : bytes) {
	    System.out.println((char)by);
	}
	
//	ProxyServer server = new ProxyServer();
//	
//	try {
//	    server.init();
//	    server.begin();
//	    
//	} catch (IOException e) {
//	    e.printStackTrace();
//    	}
	
	System.out.println("End.");
	
    }

}
