/** Mirror class which serves as a proxy for ingress and egress connections */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Mirror {
	
	public Mirror() {	
	}
	
	public static void usage() {
        System.err.println("Usage: java Mirror <daemon alias> <configuration file>\n");
        System.exit(1);
    }

	public static void main(String[] args) throws IOException {
		// Must have 2 arguments 
        if (args.length!=2) {
            usage();
            System.exit(1);
        }
        
        // First argument (alias) must be lowercase
        if (!args[0].equals(args[0].toLowerCase())) {
            System.err.println("Daemon alias " + args[0] + " is not lowercase");
            usage();
            System.exit(1);
        }

        // Second argument must be configuration file
        File configFile = new File(args[1]);
        if (!configFile.isFile()) {
            System.err.println(args[1] + " is not a valid file name");
            usage();
            System.exit(1);
        }
		
		Configurator config = new Configurator();
		config.readConfig(configFile);
		HashMap<String, String[]> ingressHostInfo = config.getIngress();
		HashMap<String, String[]> egressHostInfo = config.getEgress();
		
		// Checks if alias provided in command line is in configuration file for both ingress and egress
		if (!ingressHostInfo.containsKey(args[0]) || !egressHostInfo.containsKey(args[0])) {
			System.err.println("Alias " + args[1] + " could not be found for ingress and/or egress");
            System.exit(1);
		}
		
		Connection conn = new Connection(config);
		conn.connectClient(args[0]);
	}

}
