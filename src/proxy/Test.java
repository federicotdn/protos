package proxy;

import java.io.IOException;

import exceptions.InvalidCommandException;
import pop3.POP3CommandParser;

public class Test {

    public static void main(String[] args) throws IOException, InvalidCommandException {
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
