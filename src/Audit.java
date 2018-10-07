// Class which generates audit log entries

import java.net.Socket;
import java.sql.Timestamp;

public class Audit {
	
	// Emits a line to standard out when a new connection is made
	public static void recordNewConnection(Socket sock, String serverAddress, int serverPort) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String clientAddress = sock.getInetAddress().getHostAddress();
		int clientPort = sock.getPort();
		String newConnection = timestamp + " | Client: " + clientAddress + " " + clientPort +
						       ", Server: " + serverAddress + " " + serverPort;
		System.out.println(newConnection);
	}
	
	// Emits a line to standard out when a connection is closed
	public static void recordClosedConnection() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp + " | Connection closed");
	}

}
