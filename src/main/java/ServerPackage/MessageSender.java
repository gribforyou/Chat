package ServerPackage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageSender extends Remote {
    void sendChatMessage(String sender, String message)  throws RemoteException;
    boolean registerNick(String nick)  throws RemoteException;
}
