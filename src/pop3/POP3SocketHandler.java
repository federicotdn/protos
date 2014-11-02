package pop3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

import javax.xml.bind.JAXBException;

import exceptions.InvalidCommandException;
import proxy.ServerState;
import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {

    private static int POP3_PORT = 110;
    
    private static final String POP3_QUIT = "QUIT";
    private static final String POP3_USER = "USER";
    private static final String POP3_PASS = "PASS";
    private static final String POP3_STAT = "STAT";
    private static final String POP3_LIST = "LIST";
    private static final String POP3_RETR = "RETR";
    private static final String POP3_DELE = "DELE";
    private static final String POP3_CAPA = "CAPA";
    private static final String POP3_NOOP = "NOOP";
    private static final String POP3_RSET = "RSET";
    
    private static final String POP3_OK = "+OK";
    private static final String POP3_ERR = "-ERR";
    
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
	socketState.updateClientSubscription(key);

	serverState.setSocketHandler(clientChannel, this);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {
	
	SocketChannel readChannel = (SocketChannel) key.channel();
	POP3SocketState state = (POP3SocketState) key.attachment();

	if (readChannel == state.getClientChannel()) {
	    handleClientRead(key, state);
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
	 
	copyServerToClient(state);
	
	state.updateClientSubscription(key);
	state.updateServerSubscription(key);
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

	boolean finished = false;
	
	clientReadChannel(state);
	
	while (!finished) {
	    
	    clientReadLine(state);
	    
	    if (state.isCurrentLineReady()) {

		String line = state.getCurrentLine().toString();
		state.resetCurentLine();
		
		try {

		    POP3Line com = pop3Parser.commandFromString(line);
		    
		    handleClientCommand(key, state, com);

		} catch (InvalidCommandException e) {

		    sendClientError(state, e.getMessage());

		}

	    } else if (state.hasError()) {

		String error = state.removeLastError();
		sendClientError(state, error);

	    } else {

		finished = true;

	    }

	}
	
	state.updateClientSubscription(key);
    }
    
    private void handleClientCommand(SelectionKey key, POP3SocketState state, POP3Line com) throws IOException {

	switch (com.getCommand()) {

	case POP3_CAPA:
	    
	    List<String> capaList = serverState.getConfig().getCapaList();
	    
	    sendClientOK(state);
	    
	    for (String capa : capaList) {
		
		appendToClient(state, capa);
		
	    }
	    
	    appendToClient(state, ".");

	    break;
	    
	case POP3_USER:
	    
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
	    state.setServerChannel(pop3ServerChannel);
	    state.setPop3ServerHostname(server);
	    state.setLastUSERCommand(com);
	    pop3ServerChannel.connect(new InetSocketAddress(server, POP3_PORT));
	    
	    serverState.setSocketHandler(pop3ServerChannel, this);
	    pop3ServerChannel.register(key.selector(), SelectionKey.OP_CONNECT, state);
	    
	    break;
	    
	case POP3_PASS:
	    
	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.updateServerSubscription(key);
		
	    } else {
		
		sendClientError(state, "No username given, or connection to server failed.");
		
	    }
	    
	    break;
	    
	case POP3_QUIT:
	    
	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.updateServerSubscription(key);
		
	    } else {
		
		sendClientOK(state);
		state.setClosing(true);
	    }   
	    
	    break;
	    
	case POP3_RSET:
	case POP3_STAT:
	case POP3_LIST:
	case POP3_RETR:
	case POP3_DELE:
	case POP3_NOOP:

	    if (state.isServerConnected()) {
		
		appendToServer(state, com.getCommandString());
		state.updateServerSubscription(key);
		
	    } else {
		
		sendClientError(state, "Proxy: handle command locally.");
		
	    }

	    break;

	default: //RSET, STAT, LIST, RETR, DELE, NOOP
	    
	    sendClientError(state, "Invalid command: " + com.getCommandString());
	    
	    break;
	    
	}
	
	state.updateClientSubscription(key);

    }

    private void clientReadLine(POP3SocketState state) {
	
	SocketChannel clientChannel = state.getClientChannel();
	ByteBuffer buf = state.readBufferFor(clientChannel);
	char lastChar = 0;
	StringBuffer sb = state.getCurrentLine();
	
	while (buf.hasRemaining()) {

	    char ch = (char) buf.get();
	    
	    if (isPrintableAscii(ch)) {
		
		//Restar 2 a getMaxRequestLen para no incluir \r\n
		if (sb.length() > pop3Parser.getMaxRequestLen() - 2) {
		    
		    //Mensaje demasiado largo
		    skipBufferLine(buf);
		    state.resetCurentLine();
		    state.setLastError("Request was too long.");
		    break;
		    
		} else {
		    
		    sb.append(ch);
		    
		}
		
	    } else if (ch != '\r' && ch != '\n') {
		
		//Caracter ASCII invalido
		skipBufferLine(buf);
		state.resetCurentLine();
		state.setLastError("Invalid ASCII character received.");
		break;
		
	    } else if (lastChar == '\r' && ch == '\n') {
		
		state.setCurrentLineReady(true);
		break;
		
	    }
	    
	    lastChar = ch;
	}	
    }
    
    private void skipBufferLine(ByteBuffer buf) {
	char lastChar = 0;
	
	while (buf.hasRemaining()) {	
	    char ch = (char) buf.get();
	    if (lastChar == '\r' && ch == '\n') {
		return;
	    }
	    lastChar = ch;
	}
    }
    
    private boolean isPrintableAscii(char ch) {
	return ch >= 32 && ch <= 126;
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
	
	if (state.isClosing() && !buf.hasRemaining()) {
	    
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
	
	StringBuffer msg = new StringBuffer(POP3_OK);
	msg.append(" ").append(serverState.getConfig().getGreeting());
	
	appendToClient(state, msg.toString());
    }

    private void sendClientError(POP3SocketState state, String error) throws IOException {
	
	StringBuffer msg = new StringBuffer(POP3_ERR);
	msg.append(" ").append(error);
	
	appendToClient(state, msg.toString());
	
    }
    
    private void sendClientOK(POP3SocketState state) throws IOException {
	
	String msg = POP3_OK;
	appendToClient(state, msg);
	
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
	
	boolean connected = pop3ServerChannel.finishConnect();
	if (connected) {
	    
	    state.setServerConnected(true);
	    state.initServerBuffers();
	    
	    String lastCommand = state.getLastUSERCommand().getCommandString();
	    appendToServer(state, lastCommand);
	    
	    pop3ServerChannel.register(key.selector(), SelectionKey.OP_READ, state);
	    
	} else {
	    //mandarle -ERR al usuario
	}
    }

}
