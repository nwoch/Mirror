// Class which creates listening and connection sockets and two new threads for each new client connection

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
	
	// Make new connection to a client
	public void connectClient(String alias) {
		String ingressAddress = ingressHostInfo.get(alias)[0];
		int ingressPort = Integer.parseInt(ingressHostInfo.get(alias)[1]);
		String egressAddress = egressHostInfo.get(alias)[0];
		int egressPort = Integer.parseInt(egressHostInfo.get(alias)[1]);
		ServerSocket srvSock;
        InetAddress serverAddress;
        
        // Setup the server side connection data
        try {
            serverAddress = InetAddress.getByName(ingressAddress);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // Make the server socket with a maximum queue of 16 connections
        try {
			srvSock = new ServerSocket(ingressPort, 16, serverAddress);
		} catch (IOException e) {
			System.err.println("Could not create socket.");
			e.printStackTrace();
            System.exit(1);
            return;
		}
        
        // Read and handle connections forever
        while(true) {
            // Get the next connection
        	// Create new socket that is connected to a particular client
			try {
				Socket listeningSock = srvSock.accept();
				
				// Record new connection in audit log
				if (listeningSock.isConnected()) {
					Audit.recordNewConnection(listeningSock, egressAddress, egressPort);
				}
				
				// Make new connection to server from Mirror
				Socket connectingSock = connectServer(alias, egressAddress, egressPort);
				
				// Create two new threads for new connection
				createThreads(listeningSock, connectingSock);
			} catch (IOException e1) {
				 System.err.println("Could not accept connection.");
				 e1.printStackTrace();
		         System.exit(1);
			}
		}
     }
	
	private Socket connectServer(String alias, String egressAddress, int egressPort) {
        InetAddress serverAddress = null;
        InetSocketAddress endpoint;
        Socket connectingSock;
        
        // Setup the server side connection data
        try {
			serverAddress = InetAddress.getByName(egressAddress);
		} catch (UnknownHostException e1) {
			System.err.println("Bad server address");
			e1.printStackTrace();
			System.exit(1);
		}
        endpoint = new InetSocketAddress(serverAddress, egressPort);
        
        //// Make the TCP connection
        connectingSock = new Socket();
        
        // Make the connection to server
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
		
        // Start the threads
        rel.start();
        ret.start();
        
        // Wait until the threads have finished (if necessary)
//        try {
//            rel.join();
//            ret.join();
//        } 
       
	}
	
}
