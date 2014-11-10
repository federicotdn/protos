package config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="l33tTransformations")
@XmlAccessorType(XmlAccessType.FIELD)
public class L33tTransformationsMap {
	private Map<String, String> transformations = new HashMap<String, String>();

	public Map<String, String> getTransformations() {
		return transformations;
	}

	public L33tTransformationsMap(Map<String, String> transformations) {
		super();
		this.transformations = transformations;
	}
	
	public L33tTransformationsMap() {
		
	}
	
	public void validate() {
		for (String s: transformations.keySet()) {
			char c = s.charAt(0);
			if (c < 32 || c > 126) {
				throw new IllegalArgumentException(
						"Invalid transformation in l33tTransformations.xml");
			}
		}
	}
	 
    
}
