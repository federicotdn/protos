package rcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.StateEdit;
import javax.xml.bind.JAXBException;

import config.XMLManager;
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
				sendErrorMessage(state.getOutBuffer(),
						RCPParser.ERROR_LINE_LEN, "Request too long.");

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
			handlePass(state, commandLine);
			break;
		case GET:
			handleGet(state, commandLine);
			break;
		case SET:
			handleSet(state, commandLine);
			break;
		case DELETE:
			handleDelete(state, commandLine);
			break;
		case QUIT:
			handleQuit(state);
			break;
		default:
			break;
		}

	}

	private void handlePass(RCPSocketState state, RCPLine commandLine) throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		if (state.isLoggedIn()) {
			sendErrorMessage(buf, RCPParser.ERROR_INVALID_CMD,
					"Invalid command.");
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
	}

	private void handleGet(RCPSocketState state, RCPLine commandLine)
			throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		if (!state.isLoggedIn()) {
			sendErrorMessage(buf, RCPParser.ERROR_UNAUTH,
					"Unauthorized access.");
			return;
		}
		String getValue = null;
		switch (commandLine.getKeyword()) {
		case BYTES:
			getValue = serverState.getStats().getBytes().toString();
			break;
		case ACCESS_COUNT:
			getValue = serverState.getStats().getAccessCount().toString();
			break;
		case MPLX:
			getValue = serverState.getConfig().isMultiplexingEnabled() ? RCPParser.ENABLE_KW
					: RCPParser.DISABLE_KW;
			break;
		case L33T:
			getValue = serverState.getConfig().isL33tEnabled() ? RCPParser.ENABLE_KW
					: RCPParser.DISABLE_KW;
			break;
		case BUFFER_SIZE:
			getValue = serverState.getConfig().getPOP3BufSize().toString();
			break;
		case DEFAULT:
			getValue = serverState.getConfig().getDefaultPOP3Server();
			break;
		case USER:
			if (serverState.getConfig().getUsers()
					.containsKey(commandLine.getParameters()[0])) {
				getValue = serverState.getConfig().getUsers()
						.get(commandLine.getParameters()[0]);
			} else {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_USER,
						"Invalid user.");
			}
			break;
		case L33T_TRANSF:
			String l33tChar = commandLine.getParameters()[0];
			if (serverState.getConfig().getL33tTransformations().containsKey(l33tChar)) {
				getValue = serverState.getConfig().getL33tTransformations().get(l33tChar);
			} else {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_L33T,
						"Invalid l33t transformation.");
			}
			break;
		case STATS:
			sendOKMessage(buf, "");
			appendToBuffer(
					buf,
					getValueToString(RCPKeywordEnum.BYTES, serverState
							.getStats().getBytes().toString()));
			appendToBuffer(
					buf,
					getValueToString(RCPKeywordEnum.ACCESS_COUNT, serverState
							.getStats().getAccessCount().toString()));
			appendToBuffer(buf, ".");
			break;
		case USERS:
			sendOKMessage(buf, "");
			Map<String, String> users = serverState.getConfig().getUsers();
			for (String user : users.keySet()) {
				appendToBuffer(buf, user + " " + users.get(user));
			}
			appendToBuffer(buf, ".");
			break;
		case L33T_TRANSFS:
			sendOKMessage(buf, "");
			Map<String, String> transformations = serverState.getConfig()
					.getL33tTransformations();
			for (String transf : transformations.keySet()) {
				appendToBuffer(buf, transf + " " + transformations.get(transf));
			}
			appendToBuffer(buf, ".");
			break;
		default:
			sendErrorMessage(buf, RCPParser.ERROR_INVALID_KW, "Invalid keyword");
			break;
		}

		if (getValue != null) {
			sendData(buf, getValue);
		}
	}

	private void handleSet(RCPSocketState state, RCPLine commandLine)
			throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		if (!state.isLoggedIn()) {
			sendErrorMessage(buf, RCPParser.ERROR_UNAUTH,
					"Unauthorized access.");
			return;
		}

		switch (commandLine.getKeyword()) {
		case BUFFER_SIZE:
			int prevBufSize = 0;
			try {
				int bufSize = Integer.parseInt(commandLine.getParameters()[0]);
				if (bufSize > RCPParser.MAX_BUF_SIZE || bufSize <= 0) {
					sendErrorMessage(buf, RCPParser.ERROR_INVALID_PARAM,
							"Invalid buffer size.");
					return;
				}
				prevBufSize = serverState.getConfig().getPOP3BufSize();
				serverState.getConfig().setPOP3BufferSize(bufSize);
				serverState.getConfig().saveParams();
				sendOKMessage(buf, "");
			} catch (NumberFormatException e) {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_PARAM,
						"Invalid parameter type.");
			} catch (JAXBException e) {
				serverState.getConfig().setPOP3BufferSize(prevBufSize);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case L33T:
			boolean prevL33t = serverState.getConfig().isL33tEnabled();
			serverState.getConfig().setL33tEnabled(
					commandLine.getParameters()[0].toUpperCase().equals(
							RCPParser.ENABLE_KW) ? true : false);
			try {
				serverState.getConfig().saveParams();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig().setL33tEnabled(prevL33t);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}

			break;
		case MPLX:
			boolean prevMultiplexing = serverState.getConfig()
					.isMultiplexingEnabled();
			serverState.getConfig().setMultiplexingEnabled(
					commandLine.getParameters()[0].toUpperCase().equals(
							RCPParser.ENABLE_KW) ? true : false);
			try {
				serverState.getConfig().saveParams();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig()
						.setMultiplexingEnabled(prevMultiplexing);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case DEFAULT:
			String prevDefault = serverState.getConfig().getDefaultPOP3Server();
			serverState.getConfig().setDefaultPOP3Server(
					commandLine.getParameters()[0]);
			try {
				serverState.getConfig().saveParams();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig().setDefaultPOP3Server(prevDefault);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case PASS:
			String prevPass = serverState.getConfig().getPassword();
			serverState.getConfig().setPassword(commandLine.getParameters()[0]);
			try {
				serverState.getConfig().saveParams();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig().setPassword(prevPass);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case L33T_TRANSF:
			String key = commandLine.getParameters()[0];
			String value = commandLine.getParameters()[1];
			String prevChar = serverState.getConfig().getL33tTransformations()
					.get(key);
			serverState.getConfig().getL33tTransformations().put(key, value);
			try {
				serverState.getConfig().saveTransformations();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				if (prevChar != null) {
					serverState.getConfig().getL33tTransformations()
							.put(key, prevChar);
				} else {
					serverState.getConfig().getL33tTransformations()
							.remove(key);
				}
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}

			break;
		case USER:
			String user = commandLine.getParameters()[0];
			String server = commandLine.getParameters()[1];
			String prevServer = serverState.getConfig().getUsers().get(user);
			serverState.getConfig().getUsers().put(user, server);
			try {
				serverState.getConfig().saveUsers();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				if (prevServer != null) {
					serverState.getConfig().getUsers().put(user, prevServer);
				} else {
					serverState.getConfig().getUsers().remove(user);
				}
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		default:
			sendErrorMessage(buf, RCPParser.ERROR_INVALID_KW, "Invalid keyword");
			break;
		}
	}

	private void handleDelete(RCPSocketState state, RCPLine commandLine)
			throws IOException {
		ByteBuffer buf = state.getOutBuffer();
		if (!state.isLoggedIn()) {
			sendErrorMessage(buf, RCPParser.ERROR_UNAUTH,
					"Unauthorized access.");
			return;
		}

		switch (commandLine.getKeyword()) {
		case ACCESS_COUNT:
			int prevCount = serverState.getStats().getAccessCount();
			serverState.getStats().setAccessCount(0);
			try {
				serverState.getStats().save();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getStats().setAccessCount(prevCount);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case BYTES:
			int prevBytes = serverState.getStats().getBytes();
			serverState.getStats().setBytes(0);
			try {
				serverState.getStats().save();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getStats().setBytes(prevBytes);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case STATS:
			prevBytes = serverState.getStats().getBytes();
			prevCount = serverState.getStats().getAccessCount();
			serverState.getStats().setAccessCount(0);
			serverState.getStats().setBytes(0);
			try {
				serverState.getStats().save();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getStats().setBytes(prevBytes);
				serverState.getStats().setAccessCount(prevCount);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case USERS:
			Map<String, String> users = serverState.getConfig().getUsers();
			serverState.getConfig().setUsers(new HashMap<String, String>());
			try {
				serverState.getConfig().saveUsers();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig().setUsers(users);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case L33T_TRANSFS:
			Map<String, String> transformations = serverState.getConfig()
					.getL33tTransformations();
			serverState.getConfig().setL33tTransformations(
					new HashMap<String, String>());
			try {
				serverState.getConfig().saveTransformations();
				sendOKMessage(buf, "");
			} catch (JAXBException e) {
				serverState.getConfig().setUsers(transformations);
				sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
						"Internal error.");
			}
			break;
		case USER:
			String user = commandLine.getParameters()[0];
			if (serverState.getConfig().getUsers().containsKey(user)) {
				String prevServer = serverState.getConfig().getUsers()
						.get(user);
				serverState.getConfig().getUsers().remove(user);
				try {
					serverState.getConfig().saveUsers();
					sendOKMessage(buf, "");
				} catch (JAXBException e) {
					serverState.getConfig().getUsers().put(user, prevServer);
					sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
							"Internal error.");
				}
			} else {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_USER,
						"Invalid user.");
			}
			break;
		case L33T_TRANSF:
			String l33tChar = commandLine.getParameters()[0];
			if (serverState.getConfig().getL33tTransformations()
					.containsKey(l33tChar)) {
				String transf = serverState.getConfig()
						.getL33tTransformations().get(l33tChar);
				serverState.getConfig().getL33tTransformations()
						.remove(l33tChar);
				try {
					serverState.getConfig().saveTransformations();
					sendOKMessage(buf, "");
				} catch (JAXBException e) {
					serverState.getConfig().getL33tTransformations()
							.put(l33tChar, transf);
					sendErrorMessage(buf, RCPParser.ERROR_INTERNAL,
							"Internal error.");
				}
			} else {
				sendErrorMessage(buf, RCPParser.ERROR_INVALID_USER,
						"Invalid user.");
			}
			break;
		default:
			sendErrorMessage(buf, RCPParser.ERROR_INVALID_KW, "Invalid keyword");
			break;
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
	
	private void handleQuit(RCPSocketState state) throws IOException {
		sendOKMessage(state.getOutBuffer(), "Goodbye.");
		state.setClosing(true);
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
