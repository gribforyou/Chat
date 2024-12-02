package ClientPackage;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) {
    	  if (args.length < 4) {
              System.err.println("Usage: java ClientMain <serverName> <serverIp> <serverPort> <clientPort>");
              System.exit(1);
          }

        	  String serverName = args[0];
              String serverIp = args[1];
              int serverPort = Integer.parseInt(args[2]);
              int clientPort = Integer.parseInt(args[3]);

          Client client = new Client(serverName, serverIp, serverPort, clientPort);
         
        client.run();
    }
}

