package pop3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.List;

import javax.xml.bind.JAXBException;

import proxy.ServerState;
import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {

    public static final int POP3_PORT = 110;
    
    private POP3CommandParser pop3Parser;
    private ServerState serverState;

    public POP3SocketHandler(ServerState serverState) throws IOException, JAXBException {

	pop3Parser = new POP3CommandParser();
	this.serverState = serverState;
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {

	ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();

	SocketChannel clientChannel = listenChannel.accept();
	clientChannel.configureBlocking(false);

	int bufSize = serverState.getConfig().getPOP3BufSize();
	
	POP3SocketState socketState = new POP3SocketState(clientChannel, bufSize);
	sendClientGreeting(socketState);
	socketState.setSocketStatus(StatusEnum.WRITE_CLIENT);
	socketState.updateClientSubscription(key);

	serverState.setSocketHandler(clientChannel, this);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {

	SocketChannel readChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();
	
	if (readChannel == state.getClientChannel()) {

	    clientReadChannel(state);
	    handleClientRead(key, state);
	    state.updateClientSubscription(key);

	} else {
	    handleServerRead(key, state);
	}
    }
    
    private void handleServerRead(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel serverChannel = state.getServerChannel();
	ByteBuffer serverInBuf = state.readBufferFor(serverChannel);
	
	serverInBuf.compact();
	serverChannel.read(serverInBuf);
	serverInBuf.flip();
	
	if (state.hasStatus(StatusEnum.READ_SERVER)) {
	    readServerGreeting(key, state);
	    
	} else {
	    
	    copyServerToClient(state);    
	    
	}
	
	state.updateClientSubscription(key);
	state.updateServerSubscription(key);
    }
    
    private void readServerGreeting(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel serverChannel = state.getServerChannel();
	ByteBuffer serverInBuf = state.readBufferFor(serverChannel);
	StringBuffer sb = state.getServerGreeting();
	
	while (serverInBuf.hasRemaining()) {
	    Character lastChar = state.getGreetingLastChar();
	    char ch = (char) serverInBuf.get();
	    
	    sb.append(ch);
	    
	    if (lastChar != null && lastChar == '\r' && ch == '\n') {

		if (sb.toString().startsWith(CommandEnum.OK.toString())) {
		    sendClientOK(state, "Connected to POP3 server.");
		    state.setSocketStatus(StatusEnum.WRITE_CLIENT);
		    return;
		} else {
		    
		    disconnectServerWithError(key, state);
		    return;
		    
		}
	    }
	    
	    if (!isLineValid(sb, POP3CommandParser.MAX_RESP_LEN)) {
		
		disconnectServerWithError(key, state);
		return;
	    }
	}
	
    }
    
    private void disconnectServerWithError(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel serverChannel = state.getServerChannel();
	sendClientError(state, "Invalid response from POP3 server.");
	state.setSocketStatus(StatusEnum.WRITE_CLIENT);

	state.resetServerSettings();
	key.cancel();
	serverChannel.close();
    }
    
    private void copyServerToClient(POP3SocketState state) {
	
	ByteBuffer clientBuf = state.writeBufferFor(state.getClientChannel());
	ByteBuffer serverBuf = state.readBufferFor(state.getServerChannel());
	
	//TODO: mejorar logica de lectura y escritura a los buffers
	clientBuf.compact();
	
	while (serverBuf.hasRemaining()) {
	    
	    clientBuf.put(serverBuf.get());
	    
	}
	
	clientBuf.flip();
	
    }

    private void handleClientRead(SelectionKey key, POP3SocketState state) throws IOException {

	clientReadLine(state);

	if (state.isCurrentLineReady()) {

	    StringBuffer line = state.getCurrentLine();
	    state.resetCurentLine();

	    POP3Line com = pop3Parser.commandFromString(line);

	    handleClientCommand(key, state, com);

	} else if (state.hasLineError()) {
	    
	    state.setLineError(false);
	    sendClientError(state, "Command was too long.");
	    state.setSocketStatus(StatusEnum.WRITE_CLIENT);
	    
	} else {
	    
	    state.setSocketStatus(StatusEnum.READ_CLIENT);
	    
	}
    }
    
    private void handleClientCommand(SelectionKey key, POP3SocketState state, POP3Line com) throws IOException {

	//case: INVALID COMMAND
	if (com.getCommand() == null) {
	    
	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.setSocketStatus(StatusEnum.WRITE_SERVER);
		state.updateServerSubscription(key);
		
	    } else {
		
		String error = com.getError();
		error = (error == null) ? "" : error;
		sendClientError(state, error);
		state.setSocketStatus(StatusEnum.WRITE_CLIENT);
	    }
	    
	    return;
	}
	
	switch (com.getCommand()) {

	case CAPA:
	    
	    List<String> capaList = serverState.getConfig().getCapaList();
	    
	    sendClientOK(state);
	    
	    for (String capa : capaList) {
		appendToClient(state, capa);
	    }
	    
	    appendToClient(state, ".");
	    state.setSocketStatus(StatusEnum.WRITE_CLIENT);
	    break;
	    
	case USER:
	    
	    String user = com.getParams()[0];
	    String server = serverState.getUserPOP3Server(user);
	    SocketChannel oldServerChannel = state.getServerChannel();
	    String oldServerHostname = state.getPop3ServerHostname();
	    
	    if (state.isServerConnected()) {
		
		// Habia una coneccion a otro servidor
		if (!oldServerHostname.equals(server)) {
		    
		    oldServerChannel.keyFor(key.selector()).cancel();
		    oldServerChannel.close();
		    serverState.removeSocketHandler(oldServerChannel);
		    state.resetServerSettings();
		    
		} else {
		    
		    // Habia una coneccion al mismo servidor
		    appendToServer(state, com.getCommandString());
		    state.updateServerSubscription(key);
		    
		    break;
		}
		
	    } else {
		
		if (oldServerHostname != null) {
		    
		    if (oldServerHostname.equals(server)) {
			// Habia una coneccion pendiente al mismo servidor
			state.setLastUSERCommand(com);
			break;
			
		    } else {
			// Habia una coneccion pendiente a otro servidor
			oldServerChannel.keyFor(key.selector()).cancel();
			serverState.removeSocketHandler(oldServerChannel);
			state.resetServerSettings();	
		    }
		    
		}
		
	    }
	    
	    SocketChannel pop3ServerChannel = SocketChannel.open();
	    pop3ServerChannel.configureBlocking(false);
	    
	    try {
		pop3ServerChannel.connect(new InetSocketAddress(server, POP3_PORT));
	    } catch (UnresolvedAddressException e) {
		
		sendClientError(state, "Unable to connect to POP3 server.");
		state.setSocketStatus(StatusEnum.WRITE_CLIENT);
		break;
	    }
	    
	    state.setServerChannel(pop3ServerChannel);
	    state.setPop3ServerHostname(server);
	    state.setLastUSERCommand(com);
	    serverState.setSocketHandler(pop3ServerChannel, this);
	    pop3ServerChannel.register(key.selector(), SelectionKey.OP_CONNECT, state);
	    
	    break;
	    
	case QUIT:
	    
	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.setSocketStatus(StatusEnum.WRITE_SERVER);
		state.updateServerSubscription(key);
		
	    } else {
		
		sendClientOK(state);
		state.setSocketStatus(StatusEnum.WRITE_CLIENT); //TODO: cambiar?
		state.setClosing(true);
	    }   
	    
	    break;
	    
	case RSET:
	case STAT:
	case LIST:
	case RETR:
	case DELE:
	case NOOP:
	case PASS:

	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.setSocketStatus(StatusEnum.WRITE_SERVER);
		state.updateServerSubscription(key);
		
	    } else {
		
		sendClientError(state, "Invalid command.");
		state.setSocketStatus(StatusEnum.WRITE_CLIENT);
	    }

	    break;
	}
    }

    private void clientReadLine(POP3SocketState state) {
	
	SocketChannel clientChannel = state.getClientChannel();
	ByteBuffer buf = state.readBufferFor(clientChannel);
	StringBuffer sb = state.getCurrentLine();
	
	if (state.isCurrentLineInvalid()) {
	    skipBufferLine(state);
	    return;
	}
	
	while (buf.hasRemaining()) {
	    
	    char ch = (char) buf.get();
	    Character lastChar = state.getLineLastChar();
	    
	    sb.append(ch);
	    
	    if ((lastChar != null && lastChar == '\r' && ch == '\n')) {
		
		state.setCurrentLineReady(true);
		break;
	    }

	    if (!isLineValid(sb, POP3CommandParser.MAX_REQ_LEN)) {
		
		state.resetCurentLine();
		state.setCurrentLineInvalid(true);
		skipBufferLine(state);
		
		break;
		
	    }
	}	
    }
    
    private boolean isLineValid(StringBuffer sb, int maxLen) {

	int lineLen = sb.length();
	
	if (lineLen == 0) {
	    return true;
	}
	
	char lastChar = sb.charAt(lineLen - 1);

	if (lineLen == maxLen - 1) {

	    if (lastChar != '\r') {

		return false;

	    }

	} else if (lineLen == maxLen) {

	    if (lastChar != '\n') {

		return false;

	    }

	} else if (lineLen > maxLen) {
	    return false;
	}
	
	return true;

    }
    
    private void skipBufferLine(POP3SocketState state) {

	SocketChannel clientChannel = state.getClientChannel();
	ByteBuffer buf = state.readBufferFor(clientChannel);
	StringBuffer sb = state.getCurrentLine();
	
	while (buf.hasRemaining()) {
	    
	    char ch = (char) buf.get();
	    Character lastChar = state.getLineLastChar();

	    if (ch == '\r') {
		sb.append(ch);
	    }
	    
	    if (lastChar != null && lastChar == '\r' && ch == '\n') {
		state.setCurrentLineInvalid(false);
		state.setLineError(true);
		state.resetCurentLine();
		return;
	    }
	}
    }
    
    private void clientReadChannel(POP3SocketState state) throws IOException {

	SocketChannel clientChannel = state.getClientChannel();
	ByteBuffer buf = state.readBufferFor(clientChannel);
	
	buf.compact();
	
	if (!buf.hasRemaining()) {
	    buf.clear();
	}

	clientChannel.read(buf);
	
	buf.flip();
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

	SocketChannel writeChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();

	if (writeChannel == state.getClientChannel()) {
	    handleClientWrite(key, state);
	} else {
	    handleServerWrite(key, state);
	}

    }
    
    private void handleClientWrite(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel clientChannel = state.getClientChannel();
	ByteBuffer buf = state.writeBufferFor(clientChannel);
	
	clientChannel.write(buf);
	
	if (buf.hasRemaining()) {
	    state.updateClientSubscription(key);
	    return;
	}
	
	if (state.isClosing() && !buf.hasRemaining()) {
	    
	}
	
	ByteBuffer readBuf = state.readBufferFor(clientChannel);
	
	if (state.hasStatus(StatusEnum.WRITE_CLIENT)) {

	    if (readBuf.hasRemaining()) {

		handleClientRead(key, state);

	    } else {
		
		state.setSocketStatus(StatusEnum.READ_CLIENT);
		
	    }
	}
	
	state.updateClientSubscription(key);
    }
    
    private void handleServerWrite(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel serverChannel = state.getServerChannel();
	ByteBuffer buf = state.writeBufferFor(serverChannel);
	
	serverChannel.write(buf);
	state.updateServerSubscription(key);
    }
    
    private void sendClientGreeting(POP3SocketState state) throws IOException {
	
	StringBuffer msg = new StringBuffer(CommandEnum.OK.toString());
	msg.append(" ").append(serverState.getConfig().getGreeting());
	
	appendToClient(state, msg.toString());
    }

    private void sendClientError(POP3SocketState state, String error) throws IOException {
	
	StringBuffer msg = new StringBuffer(CommandEnum.ERR.toString());
	msg.append(" ").append(error);
	
	appendToClient(state, msg.toString());
	
    }
    
    private void sendClientOK(POP3SocketState state) throws IOException {
	
	String msg = CommandEnum.OK.toString();
	appendToClient(state, msg);
	
    }
    
    private void sendClientOK(POP3SocketState state, String additional) throws IOException {
	
	StringBuffer msg = new StringBuffer(CommandEnum.OK.toString());
	msg.append(" ").append(additional);
	
	appendToClient(state, msg.toString());
	
    }
    
    private void appendToClient(POP3SocketState state, String msg) throws IOException {
	
	ByteBuffer buf = state.writeBufferFor(state.getClientChannel());
	appendToBuffer(buf, msg);
	
    }
    
    private void appendToServer(POP3SocketState state, String msg) throws IOException {
	
	ByteBuffer buf = state.writeBufferFor(state.getServerChannel());
	appendToBuffer(buf, msg);
	
    }
    
    private void appendToBuffer(ByteBuffer buf, String msg) throws IOException {
	
	buf.compact();
	buf.put(msg.getBytes());
	buf.put("\r\n".getBytes());
	buf.flip();
    }

    @Override
    public void handleConnect(SelectionKey key) throws IOException {

	SocketChannel pop3ServerChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();

	try {
	    
	    boolean connected = pop3ServerChannel.finishConnect();
	    if (connected) {

		state.setServerConnected(true);
		state.initServerBuffers();

		String lastCommand = state.getLastUSERCommand()
			.getCommandString();
		appendToServer(state, lastCommand);

		state.setSocketStatus(StatusEnum.READ_SERVER);
		state.updateServerSubscription(key);

	    } else {

		abortServerConnection(key, state);

	    }
	    
	} catch (IOException e) {
	    
	    abortServerConnection(key, state);
	    
	}
    }
    
    private void abortServerConnection(SelectionKey key, POP3SocketState state) throws IOException {
	
	SocketChannel pop3ServerChannel = state.getServerChannel();
	
	state.resetServerSettings();
	state.setLastUSERCommand(null);
	key.cancel();
	serverState.removeSocketHandler(pop3ServerChannel);

	sendClientError(state, "Unable to connect to POP3 server.");
	state.setSocketStatus(StatusEnum.WRITE_CLIENT);
	state.updateClientSubscription(key);
    }

}
