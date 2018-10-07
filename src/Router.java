/**
* Thread class which reads from and writes to Mirror's sockets for a particular connection
* in order to forward a message from one host to another
*/

import java.io.IOException;
import java.net.Socket;

public class Router extends Thread {
		
	Boolean fromClient;
	Socket readSock; 
	Socket writeSock;
	
	public Router(Boolean fromClient, Socket readSock, Socket writeSock) {
		this.fromClient = fromClient;
		this.readSock = readSock;
		this.writeSock = writeSock;
	}
	
	// Thread reads a message from one host and writes it back to another host through the Mirror
	public void run() {
		byte[] rbuf = new byte[100];
		int r;
		try {
			while ((r = readSock.getInputStream().read(rbuf)) > 0) {	
				writeSock.getOutputStream().write(rbuf, 0, r);
			}
			
		    // Closes the socket if there is nothing left to read from the input stream
		    readSock.close();
		    writeSock.close();
		    
		} catch (IOException e) {
			// Exception is expected when trying to close socket that is already closed after the server responds
			if (!fromClient) {
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (fromClient) {
			Audit.recordClosedConnection();
		}
	}

}
