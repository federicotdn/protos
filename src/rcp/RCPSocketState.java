package rcp;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import pop3.POP3Line;

public class RCPSocketState {
	private ByteBuffer inBuffer;
	private ByteBuffer outBuffer;
	private SocketChannel channel;
	private static final int BUFF_CAP = 2048;
	private boolean currentLineInvalid;
	private StringBuffer currentLine;
	private boolean currentLineReady;
	private boolean loggedIn;
	private boolean closing;

	public RCPSocketState(SocketChannel channel) {
		this.channel = channel;
		this.inBuffer = ByteBuffer.allocate(BUFF_CAP);
		this.outBuffer = ByteBuffer.allocate(BUFF_CAP);
		this.inBuffer.limit(0);
		this.outBuffer.limit(0);
		currentLine = new StringBuffer();
		currentLineInvalid = false;
		loggedIn = false;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public boolean isClosing() {
		return closing;
	}

	public void setClosing(boolean closing) {
		this.closing = closing;
	}

	public boolean isCurrentLineInvalid() {
		return currentLineInvalid;
	}

	public void setCurrentLineInvalid(boolean currentLineInvalid) {
		this.currentLineInvalid = currentLineInvalid;
	}

	public StringBuffer getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(StringBuffer currentLine) {
		this.currentLine = currentLine;
	}

	public boolean isCurrentLineReady() {
		return currentLineReady;
	}

	public void setCurrentLineReady(boolean currentLineReady) {
		this.currentLineReady = currentLineReady;
	}

	public Character getLineLastChar() {
		int len = currentLine.length();

		if (len == 0) {
			return null;
		}

		return currentLine.charAt(len - 1);
	}

	public void updateSubscriptions(SelectionKey key)
			throws ClosedChannelException {
		int flags = 0;

		if (inBuffer.limit() - inBuffer.position() < inBuffer.capacity()) {
			flags = SelectionKey.OP_READ;
		}

		if (outBuffer.hasRemaining()) {
			flags |= SelectionKey.OP_WRITE;
		}

		channel.register(key.selector(), flags, this);
	}

	public ByteBuffer getInBuffer() {
		return inBuffer;
	}

	public ByteBuffer getOutBuffer() {
		return outBuffer;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public void resetCurentLine() {
		currentLine = new StringBuffer();
		setCurrentLineReady(false);
	}

}
