package ClientPackage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageVisualiser extends Remote {
    void visualiseMessage(String sender, String message) throws RemoteException;
}
