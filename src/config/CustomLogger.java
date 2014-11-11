package config;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pop3.POP3Line;
import proxy.ServerStatistics;

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

	public void logReadBytes(int bytes,  SocketChannel channel, String label) throws IOException {
		logBytes(channel, bytes, label, "read from");
	}

	public void logWrittenBytes(int bytes,  SocketChannel channel, String label) throws IOException {
		logBytes(channel, bytes, label, "written to");
	}

	public void logAction(String label,  SocketChannel channel, String action) throws IOException {
		logger.info(label + " " + (channel.isOpen() ? channel.getRemoteAddress() : "") + " " + action);
	}

	public void logDisconnection(String label,  SocketChannel channel) throws IOException {
		logAction(label, channel, "disconnected.");
	}

	public void logDisconnection( SocketChannel channel) throws IOException {
		logAction("", channel, "disconnected.");
	}

	public void logInvalidCommand(String commandString,  SocketChannel channel) throws IOException {
		logger.info("Client: " + (channel.isOpen() ? channel.getRemoteAddress() : "") + " executed invalid command: '"
				+ commandString.split(" ")[0] + "'.");
	}

	public void logConnection(String label,  SocketChannel channel) throws IOException {
		logAction(label, channel, "connected.");
	}
	
	public void logStatsSaved(ServerStatistics stats) {
		logger.info("Statistics saved to disk. Bytes: " + stats.getBytes() + " Acces count: " + stats.getAccessCount() + ".");
	}

	private void logBytes(SocketChannel channel, int bytes, String label,
			String action) throws IOException {
		if (bytes > 0) {
			logger.info(bytes + " bytes " + action + " " + label + " "
					+ (channel.isOpen() ? channel.getRemoteAddress() : "") + ".");
		}

	}

	public void logCommand(POP3Line com, SocketChannel channel) throws IOException {
		StringBuffer sb = new StringBuffer();
		if (com.getParams() != null) {
			for (String s : com.getParams()) {
				sb.append(s + " ");
			}
		}

		logger.info("Client: " + (channel.isOpen() ? channel.getRemoteAddress() : "") + " executed command '"
				+ com.getCommand()
				+ (sb.length() != 0 ? "' with parameters '" : "") + sb + "'.");
	}
}
