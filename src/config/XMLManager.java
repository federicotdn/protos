package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import proxy.ServerStatistics;

public class XMLManager {
	private static XMLManager instance;

	public static XMLManager getInstance() {
		if (instance == null) {
			instance = new XMLManager();
		}
		return instance;
	}

	private XMLManager() {
		File f = new File("config");
		if (!f.exists()) {
			f.mkdir();
		}
		copyFile("users.xml");
		copyFile("config.xml");
		copyFile("stats.xml");
		copyFile("l33tTransformations.xml");
	}

	public Map<String, String> loadUserMap() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(UserMap.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		UserMap userMap = (UserMap) jaxbUnmarshaller.unmarshal(new File(
				"config/users.xml"));
		userMap.validate();
		return userMap.getUserMap();
	}

	public ServerConfigParams loadParams() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(ServerConfigParams.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		ServerConfigParams params = (ServerConfigParams) jaxbUnmarshaller
				.unmarshal(new File("config/config.xml"));
		params.validate();
		return params;
	}

	public Map<String, String> loadTransformations() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(L33tTransformationsMap.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		L33tTransformationsMap transfMap = (L33tTransformationsMap) jaxbUnmarshaller
				.unmarshal(new File("config/l33tTransformations.xml"));
		transfMap.validate();
		return transfMap.getTransformations();
	}

	public ServerStatistics loadServerStatistics() throws JAXBException,
			IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance(ServerStatistics.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		ServerStatistics stats = (ServerStatistics) jaxbUnmarshaller
				.unmarshal(new File("config/stats.xml"));
		return stats;
	}

	public void saveParams(ServerConfigParams params) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(ServerConfigParams.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		jaxbMarshaller.marshal(params, new File("config/config.xml"));
	}

	public void saveUsers(Map<String, String> users) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(UserMap.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		UserMap userMap = new UserMap(users);
		jaxbMarshaller.marshal(userMap, new File("config/users.xml"));
	}

	public void saveTransformations(Map<String, String> transfomrations)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(L33tTransformationsMap.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		File f = new File("config/l33tTransformations.xml");
		if (!f.exists()) {
			ServerStatistics stats = new ServerStatistics();
			saveStats(stats);
		}
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		L33tTransformationsMap transfMap = new L33tTransformationsMap(
				transfomrations);
		jaxbMarshaller.marshal(transfMap, f);
	}

	public void saveStats(ServerStatistics stats) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(ServerStatistics.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(stats, new File("config/stats.xml"));
	}

	private void copyFile(String name) {
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				"resources/" + name);
		File targetFile = new File("config/" + name);
		OutputStream outStream = null;
		try {
			int c;
			outStream = new FileOutputStream(targetFile);
			while ((c = in.read()) != -1) {
				outStream.write(c);
			}
			outStream.close();
		} catch (Exception e) {
			try {
				if (outStream != null) {
					outStream.close();
				}
			} catch (IOException e2) {
				throw new RuntimeException(e2);
			}

			throw new RuntimeException("Error loading config files.");
		}

	}

}
