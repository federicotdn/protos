package proxy;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.bind.JAXBException;

import exceptions.InvalidCommandException;

public class Test {

    public static void main(String[] args) throws Exception {
	System.out.println("Hello, World!");

	ProxyServer server = new ProxyServer();
	
	try {
	    server.init();
	    server.begin();
	    
	} catch (Exception e) {
	    throw e;
    	}
	
	System.out.println("End.");
	
    }

}
