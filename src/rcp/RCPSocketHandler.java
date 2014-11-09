package rcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.swing.undo.StateEdit;

import pop3.CommandEnum;
import proxy.ServerState;
import proxy.TCPProtocol;
import exceptions.RCPException;

public class RCPSocketHandler implements TCPProtocol {

	private ServerState serverState;
	private RCPParser rcpParser;

	public RCPSocketHandler(ServerState serverState) {
		this.serverState = serverState;
		this.rcpParser = new RCPParser();
	}

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();

		SocketChannel channel = listenChannel.accept();
		channel.configureBlocking(false);

		RCPSocketState state = new RCPSocketState(channel);
		sendOKMessage(state.getOutBuffer(), "Server ready.");
		state.updateSubscriptions(key);

		serverState.setSocketHandler(channel, this);

	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		RCPSocketState state = (RCPSocketState) key.attachment();

		boolean finished = false;

		readChannel(state);

		while (!finished) {

			readLine(state);

			if (state.isCurrentLineReady()) {

				String line = state.getCurrentLine().toString();
				state.resetCurentLine();

				try {

					RCPLine com = rcpParser.commandFromString(line);
					handleCommand(key, state, com);

				} catch (RCPException e) {
					e.printStackTrace();
					sendErrorMessage(state.getOutBuffer(), e.getErrorCode(),
							e.getMessage());
				}

			} else {

				finished = true;

			}

			state.updateSubscriptions(key);
		}

	}

	private void readChannel(RCPSocketState state) throws IOException {

		SocketChannel channel = state.getChannel();
		ByteBuffer buf = state.getInBuffer();

		buf.compact();

		if (!buf.hasRemaining()) {
			buf.clear();
		}

		channel.read(buf);

		buf.flip();
	}

	private void readLine(RCPSocketState state) throws IOException {
		ByteBuffer buf = state.getInBuffer();
		StringBuffer sb = state.getCurrentLine();

		if (state.isCurrentLineInvalid()) {
			skipBufferLine(state);
		}

		while (buf.hasRemaining()) {

			char ch = (char) buf.get();
			Character lastChar = state.getLineLastChar();

			sb.append(ch);

			if ((lastChar != null && lastChar == '\r' && ch == '\n')) {

				state.setCurrentLineReady(true);
				break;
			}

			if (!isLineValid(sb)) {

				state.resetCurentLine();
				state.setCurrentLineInvalid(true);
				skipBufferLine(state);
				sendErrorMessage(state.getOutBuffer(), RCPParser.ERROR_LINE_LEN, "Request too long.");
				
				break;

			}
		}
	}

	private boolean isLineValid(StringBuffer sb) {

		int lineLen = sb.length();

		if (lineLen == 0) {
			return true;
		}

		char lastChar = sb.charAt(lineLen - 1);

		if (lineLen == RCPParser.MAX_REQUEST_LEN - 1) {

			if (lastChar != '\r') {

				return false;

			}

		} else if (lineLen == RCPParser.MAX_REQUEST_LEN) {

			if (lastChar != '\n') {

				return false;

			}

		} else if (lineLen > RCPParser.MAX_REQUEST_LEN) {
			return false;
		}

		return true;

	}

	private void skipBufferLine(RCPSocketState state) {
		ByteBuffer buf = state.getInBuffer();
		StringBuffer sb = state.getCurrentLine();

		while (buf.hasRemaining()) {

			char ch = (char) buf.get();
			Character lastChar = state.getLineLastChar();

			if (ch == '\r') {
				sb.append(ch);
			}

			if (lastChar != null && lastChar == '\r' && ch == '\n') {
				state.setCurrentLineInvalid(false);
				state.resetCurentLine();
				return;
			}
		}
	}

	private void handleCommand(SelectionKey key, RCPSocketState state,
			RCPLine commandLine) throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		switch (commandLine.getCommand()) {
		case PASS:
			if (state.isLoggedIn()) {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_CMD, "Invalid command.");
			} else {
				if (commandLine.getParameters()[0].equals(serverState.getConfig()
						.getPassword())) {
					sendOKMessage(buf, "Logged in.");
					state.setLoggedIn(true);
				} else {
					appendToBuffer(buf, "Invalid password.");
					sendErrorMessage(state.getOutBuffer(),
							RCPParser.ERROR_INVALID_PASSWORD, "Invalid password.");
				}
			}
			
			break;
		case GET:
			handleGet(state, commandLine);
			break;
		case QUIT:
			sendOKMessage(buf, "Goodbye.");
			state.setClosing(true);
			
			break;
		default:
			break;
		}

	}

	private void handleGet(RCPSocketState state, RCPLine commandLine)
			throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		if (!state.isLoggedIn()) {
			sendErrorMessage(buf, RCPParser.ERROR_UNAUTH, "Unauthorized access.");
			return;
		}
		String getValue = null;
		switch (commandLine.getKeyword()) {
		case BYTES:
			getValue = serverState.getStats()
					.getBytes().toString();
			break;
		case ACCESS_COUNT:
			getValue = serverState.getStats().getAccessCount().toString();
			break;
		case MPLX:
			getValue = serverState.getConfig().isMultiplexingState() ? RCPParser.ENABLE_KW : RCPParser.DISABLE_KW;
			break;
		case L33T:
			getValue = serverState.getConfig().isL33tEnabled() ? RCPParser.ENABLE_KW : RCPParser.DISABLE_KW;
			break;
		case BUFFER_SIZE:
			getValue = serverState.getConfig().getPOP3BufSize().toString();
			break;
		case DEFAULT:
			getValue = serverState.getConfig().getDefaultPOP3Server();
			break;
		case USER:
			if (serverState.getConfig().getUsers().containsKey(commandLine.getParameters()[0])) {
				getValue = serverState.getConfig().getUsers().get(commandLine.getParameters()[0]);
			} else {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_USER, "Invalid user.");
			}
			break;
		case STATS:
			sendOKMessage(buf, "");
			appendToBuffer(buf, getValueToString(RCPKeywordEnum.BYTES, serverState.getStats()
							.getBytes().toString()));
			appendToBuffer(buf, getValueToString(RCPKeywordEnum.ACCESS_COUNT, serverState.getStats()
							.getAccessCount().toString()));
			appendToBuffer(buf, ".");
			break;
		case USERS:
			sendOKMessage(buf, "");
			Map<String, String> users = serverState.getConfig().getUsers();
			for (String user: users.keySet()) {
				appendToBuffer(buf, user + " " + users.get(user));
			}
			appendToBuffer(buf, ".");
			break;
		default:
			break;
		}
		
		if (getValue != null) {
			sendData(buf, getValue);
		}
	}

	private String getValueToString(RCPKeywordEnum kw, String value) {
		return kw + " " + value;
	}

	private void sendData(ByteBuffer buf, String msg) throws IOException {
		sendOKMessage(buf, "");
		appendToBuffer(buf, msg);
		appendToBuffer(buf, ".");
	}

	private void sendOKMessage(ByteBuffer buf, String msg) throws IOException {
		appendToBuffer(buf, CommandEnum.OK + " " + msg);
	}

	private void sendErrorMessage(ByteBuffer buf, Integer errorCode, String msg)
			throws IOException {
		appendToBuffer(buf, CommandEnum.ERR + " " + errorCode + " " + msg);
	}

	private void appendToBuffer(ByteBuffer buf, String msg) throws IOException {
		buf.compact();
		buf.put(msg.getBytes());
		buf.put("\r\n".getBytes());
		buf.flip();
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		RCPSocketState state = (RCPSocketState) key.attachment();

		SocketChannel channel = state.getChannel();
		ByteBuffer buf = state.getOutBuffer();

		channel.write(buf);
		
		if (state.isClosing() && !buf.hasRemaining()) {
			serverState.removeSocketHandler(channel);
			channel.close();
			return;
		}

		state.updateSubscriptions(key);
	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {
		throw new UnsupportedOperationException();

	}

}
