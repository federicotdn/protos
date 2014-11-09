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

	private StringBuffer currentLine;
	private boolean currentLineReady;

	public RCPSocketState(SocketChannel channel) {
		this.channel = channel;
		this.inBuffer = ByteBuffer.allocate(BUFF_CAP);
		this.outBuffer = ByteBuffer.allocate(BUFF_CAP);
		this.inBuffer.limit(0);
		this.outBuffer.limit(0);
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
	
	public Character getLastLine() {
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
