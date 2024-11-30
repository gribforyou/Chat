package ProtocolClasses;

import ProtocolClasses.ServerMessages.AbstractServerMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    // Получение сообщения от сервера
    void receiveMessage(AbstractServerMessage message) throws RemoteException;
}
