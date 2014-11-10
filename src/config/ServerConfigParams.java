package config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import rcp.RCPParser;

/*
 * Clase instanciada usando JAXB.
 */
@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerConfigParams {
	private Integer pop3Port;
	private Integer rcpPort;
	private String greeting;
	private String pop3Host;
	private String rcpHost;
	private String defaultPOP3Server;
	private Integer pop3BufferSize;
	private String password;
	@XmlElement(defaultValue = "true")
	private boolean multiplexingEnabled;
	@XmlElement(defaultValue = "true")
	private boolean l33tEnabled;

	public Integer getPop3BufferSize() {
		return pop3BufferSize;
	}

	public void setPop3BufferSize(Integer pop3BufferSize) {
		this.pop3BufferSize = pop3BufferSize;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isMultiplexingEnabled() {
		return multiplexingEnabled;
	}

	public void setMultiplexingEnabled(boolean multiplexingEnabled) {
		this.multiplexingEnabled = multiplexingEnabled;
	}

	public boolean isL33tEnabled() {
		return l33tEnabled;
	}

	public void setL33tEnabled(boolean l33tEnabled) {
		this.l33tEnabled = l33tEnabled;
	}

	@XmlElement
	@XmlList
	private List<String> capaList;

	public List<String> getCapaList() {
		return capaList;
	}

	public void setCapaList(List<String> capaList) {
		this.capaList = capaList;
	}

	public Integer getPOP3BufferSize() {
		return pop3BufferSize;
	}

	public void setPOP3BufferSize(Integer pop3bufferSize) {
		this.pop3BufferSize = pop3bufferSize;
	}

	public Integer getPop3Port() {
		return pop3Port;
	}

	public void setPop3Port(Integer pop3Port) {
		this.pop3Port = pop3Port;
	}

	public Integer getRcpPort() {
		return rcpPort;
	}

	public void setRcpPort(Integer rcpPort) {
		this.rcpPort = rcpPort;
	}

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public String getPop3Host() {
		return pop3Host;
	}

	public void setPop3Host(String pop3Host) {
		this.pop3Host = pop3Host;
	}

	public String getRcpHost() {
		return rcpHost;
	}

	public void setRcpHost(String rcpHost) {
		this.rcpHost = rcpHost;
	}

	public String getDefaultPOP3Server() {
		return defaultPOP3Server;
	}

	public void setDefaultPOP3Server(String defaultPOP3Server) {
		this.defaultPOP3Server = defaultPOP3Server;
	}

	public void validate() {
		if (pop3Port == null || rcpPort == null || pop3Host == null
				|| greeting == null || defaultPOP3Server == null
				|| rcpHost == null || pop3BufferSize == null || pop3BufferSize > RCPParser.MAX_BUF_SIZE
				|| capaList == null || password == null) {
			throw new IllegalArgumentException(
					"Missing or invalid paramter in config.xml");
		}
	}

}
