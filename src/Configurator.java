/** Class which reads, parses, and stores data from configuration file */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Configurator {
	
	private HashMap<String, String[]> ingress = new HashMap<String, String[]>();
	private HashMap<String, String[]> egress = new HashMap<String, String[]>();
	
	// Reads and parses the configuration file
	public void readConfig(File configFile) {
		String portType;
		String alias;
		String ipAddress;
		String port;
		String currentLine;
		try {
			Scanner scan = new Scanner(configFile);
			while (scan.hasNext()) {
				currentLine = scan.nextLine();
				
				// Lines may not contain leading whitespace
				if (Character.isWhitespace(currentLine.charAt(0))) {
					System.err.println("Error: Line(s) in configuration file contain(s) leading whitespace");
		            System.exit(1);
				}
				
				// Lines starting with ‘#’ are comments and will be ignored during processing
				if (currentLine.charAt(0) == '#') {
					continue;
				}
				
				String[] tokens = currentLine.split("\\s+");
				
				// Lines must be designated as either Ingress or Egress
				portType = tokens[0];
				if (portType.equals("I") || portType.equals("E")) {
			
					// Alias must be lowercase
					alias = tokens[1];
			        if (!alias.equals(alias.toLowerCase())) {
			            System.err.println("ERROR: Mirror alias " + alias + " is not lowercase");
			            System.exit(1);
			        }
			        
			        // IP address must follow proper format
					ipAddress = tokens[2];
					if (!ipAddress.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
			            System.err.println("ERROR: IP Address " + ipAddress + " is not valid");
			            System.exit(1);
			        }
					
					// Port must be within appropriate range
					port = tokens[3];
			        if (Integer.parseInt(port) <= 0 || Integer.parseInt(port) > 65535) {
			            System.err.println("ERROR: Port " + port + " is not valid");
			            System.exit(1);
			        }
			        
			        // Configuration data: First index in hostID array represents IP address, second index represents port number
			        String[] hostID = new String[2];
			        hostID[0] = ipAddress;
			        hostID[1] = port;
			        if (portType.equals("I") && !ingress.containsKey(alias)) {
						ingress.put(alias, hostID);
			        }
			        else if (portType.equals("E") && !egress.containsKey(alias)) {
			        	egress.put(alias, hostID);
			        }
			        else {
			        	System.err.println("ERROR: There can only be one ingress and egress per alias.");
			        	System.exit(1);
					}
				}
				else {
				   System.err.println("ERROR: Each line must specify Ingress or Egress");
		           System.exit(1);
				}
			}
		} catch (FileNotFoundException exception) {
			System.err.println(exception);
			System.exit(1);
		}
	}
	
	public HashMap<String, String[]> getIngress() {
		return ingress;
	}
	
	public HashMap<String, String[]> getEgress() {
		return egress;
	}
}
