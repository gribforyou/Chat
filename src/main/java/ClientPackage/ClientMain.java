package ClientPackage;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) {
    	 if (args.length != 3) {
             System.err.println("Usage: java ClientMain <serverAddress> <serverPort> <clientPort>");
             System.exit(1);
         }
    	  String serverAddress = args[0];
          int serverPort;
          int clientPort;
          
          try {
              serverPort = Integer.parseInt(args[1]);
              clientPort = Integer.parseInt(args[2]);
          } catch (NumberFormatException e) {
              System.err.println("Ports must be valid integers.");
              System.exit(1);
              return; 
          }

          Client client = new Client(serverAddress, serverPort, clientPort);
         
        client.run();
    }
}

