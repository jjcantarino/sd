/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerSelector;

/**
 *
 * @author Bevz
 */


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class ServerSelector {
	public static void main(String[] args) throws IOException {
		Charset charset = Charset.forName("ISO-8859-1");
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer buffer = ByteBuffer.allocate(512);
		Selector selector = Selector.open();
		ServerSocketChannel server = ServerSocketChannel.open();
		server.socket().bind(new java.net.InetSocketAddress(8000));
		server.configureBlocking(false);
		SelectionKey serverkey = server.register(selector, SelectionKey.OP_ACCEPT);
		for (;;) {//infinite loop
			selector.select();
			Set keys = selector.selectedKeys();
			
			for (Iterator i = keys.iterator(); i.hasNext();) {
				SelectionKey key = (SelectionKey) i.next();
				i.remove();
				
				if (key == serverkey) {
					if (key.isAcceptable()) {
						SocketChannel client = server.accept();
						client.configureBlocking(false);
						SelectionKey clientkey = client.register(selector, SelectionKey.OP_READ);
						clientkey.attach(new Integer(0));
					}
				} else {
					SocketChannel client = (SocketChannel) key.channel();
					if (!key.isReadable())
						continue;
					int bytesread = client.read(buffer);
					if (bytesread == -1) {
						key.cancel();
						client.close();
						continue;
					}
					buffer.flip();
					String request = decoder.decode(buffer).toString();
					buffer.clear();
					if (request.trim().equals("quit")) {
						client.write(encoder.encode(CharBuffer.wrap("Bye.")));
						key.cancel();
						client.close();
					} else {
						int num = ((Integer) key.attachment()).intValue();
						String response = num + ": " + request.toUpperCase();
						client.write(encoder.encode(CharBuffer.wrap(response)));
						key.attach(new Integer(num + 1));
					}
				}
			}
		}
	}
}


