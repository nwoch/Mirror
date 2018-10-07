/** Class which creates listening and connection sockets and two new threads for each new client connection */

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Connection extends Thread {
	
	private HashMap<String, String[]> ingressHostInfo;
	private HashMap<String, String[]> egressHostInfo;
	
	public Connection(Configurator config) {
		this.ingressHostInfo = config.getIngress();
		this.egressHostInfo = config.getEgress();
	}
	
	// Makes new connections to clients
	public void connectClient(String alias) {
		String ingressAddress = ingressHostInfo.get(alias)[0];
		int ingressPort = Integer.parseInt(ingressHostInfo.get(alias)[1]);
		String egressAddress = egressHostInfo.get(alias)[0];
		int egressPort = Integer.parseInt(egressHostInfo.get(alias)[1]);
		ServerSocket srvSock;
        InetAddress serverAddress;
        
        // Sets up the server side connection data
        try {
            serverAddress = InetAddress.getByName(ingressAddress);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // Makes the server socket with a maximum queue of 16 connections
        try {
			srvSock = new ServerSocket(ingressPort, 16, serverAddress);
		} catch (IOException e) {
			System.err.println("Could not create socket.");
			e.printStackTrace();
            System.exit(1);
            return;
		}
        
        // Reads and handles connections forever
        while(true) {
			try {
				Socket listeningSock = srvSock.accept();
				
				// Records new connection in audit log
				if (listeningSock.isConnected()) {
					Audit.recordNewConnection(listeningSock, egressAddress, egressPort);
				}
				
				// Makes new connection to server from the Mirror
				Socket connectingSock = connectServer(alias, egressAddress, egressPort);
				
				// Creates two new threads for the new connection
				createThreads(listeningSock, connectingSock);
			} catch (IOException e1) {
				 System.err.println("Could not accept connection.");
				 e1.printStackTrace();
		         System.exit(1);
			}
		}
     }
	
	// Makes a new connection to the server
	private Socket connectServer(String alias, String egressAddress, int egressPort) {
        InetAddress serverAddress = null;
        InetSocketAddress endpoint;
        Socket connectingSock;
        
        // Sets up the server side connection data
        try {
			serverAddress = InetAddress.getByName(egressAddress);
		} catch (UnknownHostException e1) {
			System.err.println("Bad server address");
			e1.printStackTrace();
			System.exit(1);
		}
        endpoint = new InetSocketAddress(serverAddress, egressPort);
        connectingSock = new Socket();
        
        // Makes connection to server
        try {
			connectingSock.connect(endpoint);
		} catch (IOException e1) {
			System.err.println("Could not connect to server");
			e1.printStackTrace();
			System.exit(1);
		}
        
        return connectingSock;
	}
	
	private void createThreads(Socket listeningSock, Socket connectingSock) throws IOException {
		
		Router rel = new Router(true, listeningSock, connectingSock);
		Router ret = new Router(false, connectingSock, listeningSock);
		
        // Starts the threads
        rel.start();
        ret.start();
        
        // Waits until the threads have finished (if necessary)
//        try {
//            rel.join();
//            ret.join();
//        } 
       
	}
	
}
