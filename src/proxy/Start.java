package proxy;

import java.io.File;

import config.CustomLogger;

public class Start {

	public static void main(String[] args) throws Exception {
		CustomLogger.getInsance().getLogger().info("Server started.");

		ProxyServer server = new ProxyServer();

		try {
			server.init();
			server.begin();

		} catch (Exception e) {
			throw e;
		}

		CustomLogger.getInsance().getLogger().info("Server ended.");

	}

}
