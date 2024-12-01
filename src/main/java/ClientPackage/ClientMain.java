package ClientPackage;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) {
        Client client = new Client(args[0], Integer.parseInt(args[1]), 0);
        client.run();
    }
}
