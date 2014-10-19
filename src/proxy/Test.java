package proxy;

import java.io.IOException;

import org.omg.CORBA.DynAnyPackage.InvalidValue;

public class Test {

    public static void main(String[] args) throws InvalidValue {
	System.out.println("Hello, World!");
	POP3ProxyServer server = new POP3ProxyServer();
	
	try {
	    
	    server.init();
	    server.begin();
	    
	} catch (IOException e) {
	    e.printStackTrace();
    	}
	
	System.out.println("End.");
	
    }

}
