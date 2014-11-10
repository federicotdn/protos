package proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
