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
	
	public String getPassword() {
		return params.getPassword();
	}
	
	public void setPassword(String password) {
		params.setPassword(password);
	}
	
	public boolean isMultiplexingEnabled(){
		return params.isMultiplexingEnabled();
	}
	
	public boolean isL33tEnabled() {
		return params.isL33tEnabled();
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
	
	public void setL33tEnabled(boolean enabled) {
		params.setL33tEnabled(enabled);
	}
	
	public void setMultiplexingEnabled(boolean enabled) {
		params.setMultiplexingEnabled(enabled);
	}
	
	public void setUsers(Map<String, String> users) {
		this.users = users;
	}

	public void setL33tTransformations(Map<String, String> l33tTransformations) {
		this.l33tTransformations = l33tTransformations;
	}
	
	public char getL33tTransformation(Character c) {
		String transf = l33tTransformations.get(String.valueOf(c));
		return transf == null ? c :transf.charAt(0);
	}
	
}
