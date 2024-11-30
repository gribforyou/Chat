package ServerPackage;

import ProtocolClasses.ServerMessages.ChatMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageSender extends Remote {
    void sendChatMessage(ChatMessage message) throws RemoteException;
    boolean registerNick(String nick) throws RemoteException;
}
