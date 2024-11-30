package ServerPackage;

import ProtocolClasses.Protocol;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain {
    public static final String UNIQUE_BINDING_NAME = "server.multichat";

    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        final ChatServer chatServer = new ChatServer();

        final Registry registry = LocateRegistry.createRegistry(Protocol.PORT);

        Remote stub = UnicastRemoteObject.exportObject(chatServer, 0);

        registry.bind(UNIQUE_BINDING_NAME, stub);

        new Thread(chatServer).start();
    }
}
