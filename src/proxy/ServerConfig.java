package proxy;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import config.XMLManager;
import config.ServerConfigParams;

public class ServerConfig {

	private Map<String, String> users;
	private Map<String, String> l33tTransformations;

	private ServerConfigParams params;

	private InetSocketAddress pop3Address;
	private InetSocketAddress rcpAddress;

	public ServerConfig() throws JAXBException {

		users = XMLManager.loadUserMap();
		l33tTransformations = XMLManager.loadTransformations();
		params = XMLManager.loadParams();
		pop3Address = new InetSocketAddress(params.getPop3Host(),
				params.getPop3Port());
		rcpAddress = new InetSocketAddress(params.getRcpHost(),
				params.getRcpPort());

	}

	public Map<String, String> getL33tTransformations() {
		return l33tTransformations;
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
	
	public void setDefaultPOP3Server(String server) {
		params.setDefaultPOP3Server(server);
	} 
	
	public void setPOP3BufferSize(Integer bufSize) {
		params.setPOP3BufferSize(bufSize);
	}
	
	public void saveParams() throws JAXBException {
		XMLManager.saveParams(params);
	}
	
	public void saveTransformations() throws JAXBException {
		XMLManager.saveTransformations(l33tTransformations);
	}
	
	public void saveUsers() throws JAXBException{
		XMLManager.saveUsers(users);
	}
}
