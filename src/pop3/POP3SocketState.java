package pop3;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class POP3SocketState {

	private static final int AUX_BUF_SIZE = 4096;
	public static final String SUBJECT_FIELD = "\r\nSubject:";

	private int bufSize;

	private final SocketChannel clientChannel;
	private SocketChannel pop3ServerChannel;

	private boolean serverConnected;
	private String pop3ServerHostname;

	private POP3Line lastUSERCommand;

	private ByteBuffer clientOutBuf;
	private ByteBuffer clientInBuf;
	private ByteBuffer clientAuxBuf;

	private ByteBuffer serverOutBuf;
	private ByteBuffer serverInBuf;
	private ByteBuffer serverAuxBuf;

	private StringBuffer currentLine;
	private boolean currentLineReady;
	private boolean currentLineInvalid;
	private boolean lineError;

	private StringBuffer serverGreeting;

	private int charsMatched;
	private Subject currentSubject;
	
	private L33tStateEnum l33tState;

	
	private int serverStatus, clientStatus;

	public POP3SocketState(final SocketChannel clientChannel, int bufSize) {

		serverConnected = false;
		lineError = false;
		pop3ServerHostname = null;
		lastUSERCommand = null;
		currentLine = new StringBuffer();
		currentSubject = null;
		currentLineInvalid = false;
		serverStatus = clientStatus = 0;
		this.bufSize = bufSize;

		if (clientChannel == null) {
			throw new IllegalArgumentException();
		}

		this.clientChannel = clientChannel;
		this.pop3ServerChannel = null;

		clientOutBuf = ByteBuffer.allocate(this.bufSize);
		clientInBuf = ByteBuffer.allocate(this.bufSize);
		clientAuxBuf = ByteBuffer.allocate(AUX_BUF_SIZE);

		clientOutBuf.flip();
		clientInBuf.flip();
		clientAuxBuf.flip();

		serverOutBuf = null;
		serverInBuf = null;
		serverAuxBuf = null;
	}
	
	public L33tStateEnum getL33tState() {
		return l33tState;
	}

	public void setL33tState(L33tStateEnum l33tState) {
		this.l33tState = l33tState;
	}

	public Subject getCurrentSubject() {
	    return currentSubject;
	}

	public void setCurrentSubject(Subject currentSubject) {
	    this.currentSubject = currentSubject;
	}
	
	public boolean subjectFound() {
	    return (currentSubject != null);
	}

	public int getCharsMatched() {
		return charsMatched;
	}

	public void incrementCharsMatched() {
		charsMatched++;
	}

	public void resetCharsMatched() {
		charsMatched = 0;
	}

	public StringBuffer getServerGreeting() {
		return serverGreeting;
	}

	public void setServerGreeting(StringBuffer serverGreeting) {
		this.serverGreeting = serverGreeting;
	}

	public boolean hasLineError() {
		return lineError;
	}

	public void setLineError(boolean error) {
		lineError = error;
	}

	public void printFlags() {

		for (StatusEnum st : StatusEnum.values()) {

			if (hasServerFlag(st)) {
				System.out.println("Has SERVER: " + st.name());
			}

			if (hasClientFlag(st)) {
				System.out.println("Has CLIENT: " + st.name());
			}

		}

		if (serverStatus == 0 && clientStatus == 0) {
			System.out.println("Has NO FLAGS");
		}

	}

	public boolean hasServerFlag(StatusEnum status) {
		return (serverStatus & status.getVal()) != 0;
	}

	public void enableServerFlag(StatusEnum status) {
		serverStatus |= status.getVal();
	}

	public void disableServerFlag(StatusEnum status) {
		serverStatus &= (~status.getVal());
	}

	public boolean hasClientFlag(StatusEnum status) {
		return (clientStatus & status.getVal()) != 0;
	}

	public void enableClientFlag(StatusEnum status) {
		clientStatus |= status.getVal();
	}

	public void disableClientFlag(StatusEnum status) {
		clientStatus &= (~status.getVal());
	}

	public boolean isCurrentLineInvalid() {
		return currentLineInvalid;
	}

	public void setCurrentLineInvalid(boolean currentLineInvalid) {
		this.currentLineInvalid = currentLineInvalid;
	}

	public String getPop3ServerHostname() {
		return pop3ServerHostname;
	}

	public void setPop3ServerHostname(String pop3ServerHostname) {
		this.pop3ServerHostname = pop3ServerHostname;
	}

	public boolean isServerConnected() {
		return serverConnected;
	}

	public void setServerConnected(boolean connected) {
		serverConnected = connected;
	}

	public POP3Line getLastUSERCommand() {
		return lastUSERCommand;
	}

	public void setLastUSERCommand(POP3Line com) {
		lastUSERCommand = com;
	}

	public SocketChannel getClientChannel() {
		return clientChannel;
	}

	public SocketChannel getServerChannel() {
		return pop3ServerChannel;
	}

	public void setServerChannel(SocketChannel pop3ServerChannel) {
		this.pop3ServerChannel = pop3ServerChannel;
	}

	public boolean isCurrentLineReady() {
		return currentLineReady;
	}

	public void setCurrentLineReady(boolean ready) {
		currentLineReady = ready;
	}

	public void resetCurentLine() {
		currentLine = new StringBuffer();
		setCurrentLineReady(false);
	}

	public Character getLineLastChar() {
		return stringBufferLastChar(currentLine);
	}

	public Character getGreetingLastChar() {
		return stringBufferLastChar(serverGreeting);
	}
	
//	public Character getSubjectLastChar() {
//	    	return stringBufferLastChar(currentSubject);
//	}

	private Character stringBufferLastChar(StringBuffer sb) {
		int len = sb.length();

		if (len == 0) {
			return null;
		}

		return sb.charAt(len - 1);
	}

	public StringBuffer getCurrentLine() {
		return currentLine;
	}

	public void resetServerSettings() {
		setPop3ServerHostname(null);
		setServerConnected(false);
		setServerChannel(null);
		resetCharsMatched();
		setCurrentSubject(null);
		serverStatus = 0;
	}

	public void initServerBuffers() {

		if (serverInBuf != null) {
			serverInBuf.clear();
		} else {
			serverInBuf = ByteBuffer.allocate(bufSize);
		}

		if (serverOutBuf != null) {
			serverOutBuf.clear();
		} else {
			serverOutBuf = ByteBuffer.allocate(bufSize);
		}

		if (serverAuxBuf != null) {
			serverAuxBuf.clear();
		} else {
			serverAuxBuf = ByteBuffer.allocate(AUX_BUF_SIZE);
		}

		serverGreeting = new StringBuffer();
		serverInBuf.flip();
		serverOutBuf.flip();
		serverAuxBuf.flip();
	}

	public ByteBuffer writeBufferFor(SocketChannel channel) {

		if (clientChannel == channel) {
			return clientOutBuf;
		} else if (pop3ServerChannel == channel) {
			return serverOutBuf;
		} else {
			throw new IllegalArgumentException("Buffer: socket desconocido.");
		}
	}

	public ByteBuffer readBufferFor(SocketChannel channel) {

		if (clientChannel == channel) {
			return clientInBuf;
		} else if (pop3ServerChannel == channel) {
			return serverInBuf;
		} else {
			throw new IllegalArgumentException("Buffer: socket desconocido.");
		}
	}

	public ByteBuffer auxBufferFor(SocketChannel channel) {

		if (clientChannel == channel) {
			return clientAuxBuf;
		} else if (pop3ServerChannel == channel) {
			return serverAuxBuf;
		} else {
			throw new IllegalArgumentException("Buffer: socket desconocido.");
		}
	}

	public void updateServerSubscription(SelectionKey key) throws ClosedChannelException {

		if (pop3ServerChannel == null) {
			return;
		}

		int flags = 0;

		if (hasServerFlag(StatusEnum.READ) || hasServerFlag(StatusEnum.GREETING)) {
		    flags |= SelectionKey.OP_READ;
		}

		if (hasServerFlag(StatusEnum.WRITE)) {
			if (serverOutBuf.hasRemaining() || serverAuxBuf.hasRemaining()) {
				flags |= SelectionKey.OP_WRITE;
			}
		}

		pop3ServerChannel.register(key.selector(), flags, this);
	}

	public void updateClientSubscription(SelectionKey key)
			throws ClosedChannelException {

		int flags = 0;

		if (hasClientFlag(StatusEnum.READ)) {

			flags |= SelectionKey.OP_READ;

		}

		if (hasClientFlag(StatusEnum.WRITE)) {

			if (clientOutBuf.hasRemaining() || clientAuxBuf.hasRemaining()) {
				flags |= SelectionKey.OP_WRITE;
			}

		}

		clientChannel.register(key.selector(), flags, this);
	}
}
