package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.bind.JAXBException;

import exceptions.InvalidCommandException;

public class Test {

    public static void main(String[] args) throws JAXBException, InvalidCommandException {
	System.out.println("Hello, World!");

	ProxyServer server = new ProxyServer();
	
	try {
	    server.init();
	    server.begin();
	    
	} catch (IOException e) {
	    e.printStackTrace();
    	}
	
	System.out.println("End.");
	
    }

}
