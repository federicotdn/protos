package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

public class ServerConfig {
    
    private static final int POP3_PORT = 4545;
    private static final int RCP_PORT = 4546;
    
    private static String CONFIG_FILE = "resources/proxy.properties";
    private static String GREET_KEY = "greeting";
    
    private InetSocketAddress pop3Address;
    private InetSocketAddress rcpAddress;
    private Properties properties;
    
    public ServerConfig() throws IOException {
	
	InputStream fileInput = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
	properties.load(fileInput);
	
	pop3Address = new InetSocketAddress(POP3_PORT);
	rcpAddress = new InetSocketAddress(RCP_PORT);
    }
    
    public String getGreeting() {
	return properties.getProperty(GREET_KEY);
    }
    
    public InetSocketAddress getRcpAddress() {
        return rcpAddress;
    }

    public void setRcpAddress(InetSocketAddress rcpAddress) {
        this.rcpAddress = rcpAddress;
    }
    
    public InetSocketAddress getPop3Address() {
        return pop3Address;
    }

    public void setPop3Address(InetSocketAddress pop3Address) {
        this.pop3Address = pop3Address;
    }

    public int getPOP3Port() {
	return pop3Address.getPort();
    }
    
    public int getRCPPort() {
	return rcpAddress.getPort();
    }

}
