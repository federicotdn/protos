package proxy;

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
