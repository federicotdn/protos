package proxy;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import config.XMLManager;

@XmlRootElement(name = "stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatistics {
	private Integer bytes;
	private Integer accessCount;
	
	public Integer getBytes() {
		return bytes;
	}
	public void setBytes(Integer bytes) {
		this.bytes = bytes;
	}
	public Integer getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}
	
	public void save() throws JAXBException {
		XMLManager.saveStats(this);
	}
	
	public void validate() {
		if (bytes < 0 || accessCount <0) {
			throw new IllegalArgumentException("Invalid stat in stats.xml");
		}
	}
	
}
