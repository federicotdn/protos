package proxy;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import config.ConfigLoader;
import config.ServerConfigParams;

public class ServerConfig {

    private Map<String, String> users;

    private ServerConfigParams params;

    private InetSocketAddress pop3Address;
    private InetSocketAddress rcpAddress;

    public ServerConfig() throws JAXBException {
	
	users = ConfigLoader.loadUserMap("src/resources/users.xml");
	params = ConfigLoader.loadParams("src/resources/config.xml");
	pop3Address = new InetSocketAddress(params.getPop3Host(), params.getPop3Port());
	rcpAddress = new InetSocketAddress(params.getRcpHost(), params.getRcpPort());
	
    }

    public Integer getPOP3BufSize() {
	return params.getPOP3BufferSize();
    }

    public String getGreeting() {
	return params.getGreeting();
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

    public String getDefaultPOP3Server() {
	return params.getDefaultPOP3Server();
    }

    public Map<String, String> getUsers() {
	return users;
    }

    public List<String> getCapaList() {
	return params.getCapaList();
    }

}
