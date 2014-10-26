package config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerConfigParams {
	private Integer pop3Port;
	private Integer rcpPort;
	private String greeting;
	private String pop3Host;
	private String rcpHost;
	private String defaultPOP3Server;

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
				|| rcpHost == null) {
			throw new IllegalArgumentException(
					"Missing or invalid paramter in config.xml");
		}
	}

}
