package rcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import pop3.POP3Line;
import pop3.POP3SocketState;
import proxy.ServerState;
import proxy.TCPProtocol;
import exceptions.InvalidCommandException;
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
		state.updateSubscriptions(key);

		serverState.setSocketHandler(channel, this);

	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel readChannel = (SocketChannel) key.channel();
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
//
					handleCommand(key, state, com);

				} catch (RCPException e) {

//					sendClientError(state, e.getMessage());

				}

			}  else {

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

	private void readLine(RCPSocketState state) {

	}
	
	private void handleCommand(SelectionKey key, RCPSocketState state,
			RCPLine commandLine) throws IOException {
		switch (commandLine.getCommand()) {
		case PASS:
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {
		throw new UnsupportedOperationException();

	}

}
