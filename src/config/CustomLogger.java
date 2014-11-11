package config;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pop3.POP3Line;

public class CustomLogger {
	private Logger logger;
	private static CustomLogger instance;

	public static CustomLogger getInsance() {
		if (instance == null) {
			instance = new CustomLogger();
		}
		return instance;
	}

	private CustomLogger() {
		logger = LogManager.getLogger();
	}

	public Logger getLogger() {
		return logger;
	}

	public void logReadBytes(int bytes, SocketAddress address, String label) {
		logBytes(address, bytes, label, "read from");
	}

	public void logWrittenBytes(int bytes, SocketAddress address, String label) {
		logBytes(address, bytes, label, "writen to");
	}

	public void logAction(String label, SocketAddress address, String action) {
		logger.info(label + ": " + address + " " + action);
	}

	public void logDisconnection(String label, SocketAddress address) {
		logAction(label, address, "disconnected.");
	}

	public void logInvalidCommand(String commandString, SocketAddress address) {
		logger.info("Client: " + address + " executed invalid command: '"
				+ commandString.split(" ")[0] + "'.");
	}

	public void logConnection(String label, SocketAddress address) {
		logAction(label, address, "connected.");
	}

	private void logBytes(SocketAddress address, int bytes, String label,
			String action) {
		logger.info(bytes + " bytes " + action + " " + label + " " + address
				+ ".");
	}

	public void logCommand(POP3Line com, SocketAddress address) {
		StringBuffer sb = new StringBuffer();
		if (com.getParams() != null){
			for (String s : com.getParams()) {
				sb.append(s + " ");
			}
		}
		
		logger.info("Client: " + address + " executed command '"
				+ com.getCommand()
				+ (sb.length() != 0 ? "' with parameters '" : "") + sb + "'.");
	}
}
