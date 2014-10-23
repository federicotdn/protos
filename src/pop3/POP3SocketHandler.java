package pop3;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import proxy.TCPProtocol;

public class POP3SocketHandler implements TCPProtocol {
	
	@Override
	public void handleAccept(SelectionKey key) throws IOException {

		ServerSocketChannel listenChannel = (ServerSocketChannel) key.channel();

		System.out.println("Handle Accept");

		SocketChannel clientChannel = listenChannel.accept();
		clientChannel.configureBlocking(false);

	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {

		SocketChannel readChannel = (SocketChannel) key.channel();

	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {

		SocketChannel writeChannel = (SocketChannel) key.channel();

	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {

	}

}
