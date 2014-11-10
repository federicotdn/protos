package config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import rcp.RCPParser;

/*
 * Clase instanciada usando JAXB.
 */
@XmlRootElement (name="users")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserMap {
     
    private Map<String, String> userMap = new HashMap<String, String>();
 
    public Map<String, String> getUserMap() {
        return userMap;
    }
 
    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
    }
    
    public UserMap(Map<String, String> userMap) {
    	this.userMap = userMap;
    }
    
    public UserMap() {
    	
    }
    
    public void validate() {
    	for (String key: userMap.keySet()) {
    		if (key.length() > RCPParser.MAX_PARAM_LEN || !key.matches("^[ -~]+$") || userMap.get(key).length() > RCPParser.MAX_PARAM_LEN || !userMap.get(key).matches("^[ -~]+$")) {
    			throw new IllegalArgumentException(
						"Invalid parameter in users.xml");
    		}
    	}
    }
}
