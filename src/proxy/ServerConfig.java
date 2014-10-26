package proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
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
		ConfigLoader configLoader = new ConfigLoader();
		users = configLoader.loadUserMap("src/resources/users.xml");
		params = configLoader.loadParams("src/resources/config.xml");
		pop3Address = new InetSocketAddress(params.getPop3Host(), params.getPop3Port());
		rcpAddress = new InetSocketAddress(params.getRcpHost(), params.getRcpPort());
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

	public Map<String, String> getUsers() {
		return users;
	}

}
