package proxy;

import proxy.ProxyServer;

public class Start {

	public static void main(String[] args) throws Exception {
		System.out.println("Server started.");

		ProxyServer server = new ProxyServer();

		try {
			server.init();
			server.begin();

		} catch (Exception e) {
			throw e;
		}

		System.out.println("Server end.");

	}

}
