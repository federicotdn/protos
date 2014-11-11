package proxy;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import config.CustomLogger;
import config.XMLManager;

@XmlRootElement(name = "stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerStatistics {
	private Integer bytes;
	private Integer accessCount;
	@XmlTransient
	private Integer currentBytes = 0;
	private static final int AMOUNT_BYTES = 20000;

	public ServerStatistics() {
		bytes = 0;
		accessCount = 0;
	}
	
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

	public void increaseAccessCount() {
		accessCount++;
		try {
			this.save();
		} catch (JAXBException e) {
			CustomLogger.getInsance().getLogger().error("Exception", e.getCause());
		}
	}

	public void addBytes(Integer bytes) {
		this.bytes += bytes;
		this.currentBytes += bytes;
		if (currentBytes > AMOUNT_BYTES) {
			try {
				this.save();
				currentBytes = 0;
			} catch (JAXBException e) {
				CustomLogger.getInsance().getLogger().error("Exception", e.getCause());
			}
		}
	}

	public void save() throws JAXBException {
		XMLManager.saveStats(this);
	}

	public void validate() {
		if (bytes < 0 || accessCount < 0) {
			throw new IllegalArgumentException("Invalid stat in stats.xml");
		}
	}

}
