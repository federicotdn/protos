package proxy;

import java.net.InetSocketAddress;

public class ServerConfig {
    
    private static final int POP3_PORT = 4545;
    private static final int RCP_PORT = 4546;
    
    private InetSocketAddress pop3Address;
    private InetSocketAddress rcpAddress;
    
    public ServerConfig() {
	pop3Address = new InetSocketAddress(POP3_PORT);
	rcpAddress = new InetSocketAddress(RCP_PORT);
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
