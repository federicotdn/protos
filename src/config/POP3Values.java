package config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * Clase instanciada usando JAXB.
 */
@XmlRootElement(name = "pop3")
@XmlAccessorType(XmlAccessType.FIELD)
public class POP3Values {
    
    private Integer maxCommandLen;
    private Integer minCommandLen;
    private Integer maxParamLen;
    private Integer maxRequestLen;

    private Map<String, String> commandMap = new HashMap<String, String>();
    
    public Integer getMaxRequestLen() {
        return maxRequestLen;
    }

    public void setMaxRequestLen(Integer maxRequestLen) {
        this.maxRequestLen = maxRequestLen;
    }
    
    public Integer getMaxCommandLen() {
        return maxCommandLen;
    }

    public void setMaxCommandLen(Integer maxCommandLen) {
        this.maxCommandLen = maxCommandLen;
    }

    public Integer getMinCommandLen() {
        return minCommandLen;
    }

    public void setMinCommandLen(Integer minCommandLen) {
        this.minCommandLen = minCommandLen;
    }

    public Integer getMaxParamLen() {
        return maxParamLen;
    }

    public void setMaxParamLen(Integer maxParamLen) {
        this.maxParamLen = maxParamLen;
    }
    
    public Map<String, String> getCommandMap() {
        return commandMap;
    }
 
    public void setCommandMap(Map<String, String> commandMap) {
        this.commandMap = commandMap;
    }
}
