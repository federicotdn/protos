package config;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import proxy.ServerStatistics;


public class XMLManager {
    
	public static Map<String, String> loadUserMap() throws JAXBException {
	    JAXBContext jaxbContext = JAXBContext.newInstance(UserMap.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    UserMap userMap = (UserMap) jaxbUnmarshaller.unmarshal( new File("src/resources/users.xml") );
	    return userMap.getUserMap();
	}
	
	public static ServerConfigParams loadParams() throws JAXBException {
	    JAXBContext jaxbContext = JAXBContext.newInstance(ServerConfigParams.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    ServerConfigParams params = (ServerConfigParams) jaxbUnmarshaller.unmarshal( new File("src/resources/config.xml") );
	    params.validate();
	    return params;
	}
	
	public static Map<String, String> loadTransformations() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(L33tTransformationsMap.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    L33tTransformationsMap transfMap = (L33tTransformationsMap) jaxbUnmarshaller.unmarshal( new File("src/resources/l33tTransformations.xml") );
	    return transfMap.getTransformations();
	}
	
	public static ServerStatistics loadServerStatistics() throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(ServerStatistics.class);
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    return (ServerStatistics) jaxbUnmarshaller.unmarshal( new File("src/resources/stats.xml") );
	}
	
	public static void saveParams(ServerConfigParams params) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ServerConfigParams.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	 
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 
	    jaxbMarshaller.marshal(params, new File("src/resources/config.xml"));
	}
	
	
	public static void saveUsers(Map<String, String> users) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(UserMap.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    UserMap userMap = new UserMap(users);
	    jaxbMarshaller.marshal(userMap, new File("src/resources/users.xml"));
	}
	
	public static void saveTransformations(Map<String, String> transfomrations) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(L33tTransformationsMap.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    L33tTransformationsMap transfMap = new L33tTransformationsMap(transfomrations);
	    jaxbMarshaller.marshal(transfMap, new File("src/resources/l33tTransformations.xml"));
	}
	
	public static void saveStats(ServerStatistics stats) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ServerStatistics.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    jaxbMarshaller.marshal(stats, new File("src/resources/stats.xml"));
	}
	
}
