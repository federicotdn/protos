package config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
}
