package config;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import proxy.ServerStatistics;


public class ConfigLoader {
    
	public static Map<String, String> loadUserMap(String fileName) throws JAXBException {
	    JAXBContext jaxbContext = JAXBContext.newInstance(UserMap.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    UserMap userMap = (UserMap) jaxbUnmarshaller.unmarshal( new File(fileName) );
	    return userMap.getUserMap();
	}
	
	public static ServerConfigParams loadParams(String fileName) throws JAXBException {
	    JAXBContext jaxbContext = JAXBContext.newInstance(ServerConfigParams.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    ServerConfigParams params = (ServerConfigParams) jaxbUnmarshaller.unmarshal( new File(fileName) );
	    params.validate();
	    return params;
	}
	
	public static ServerStatistics loadServerStatistics() throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(ServerStatistics.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    return (ServerStatistics) jaxbUnmarshaller.unmarshal( new File("src/resources/stats.xml") );
	}

}
