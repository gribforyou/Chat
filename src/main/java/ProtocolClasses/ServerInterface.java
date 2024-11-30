package ProtocolClasses;

import ProtocolClasses.ClientMessage.AbstractClientMessage;
import ProtocolClasses.ServerMessages.AbstractServerMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // Регистрация клиента
    boolean registerClient(String nickName, ClientInterface client) throws RemoteException;

    // Отправка сообщения от клиента
    void sendMessage(AbstractClientMessage message, String nickName) throws RemoteException;
    // Добавляем метод для получения сообщений
    AbstractServerMessage receiveMessage() throws RemoteException;
}
