package ClientPackage;

import ProtocolClasses.ClientInterface;
import ProtocolClasses.ServerMessages.AbstractServerMessage;
import java.rmi.RemoteException;

public class ClientHandler implements ClientInterface {
    @Override
    public void receiveMessage(AbstractServerMessage message) throws RemoteException {
        System.out.println("Message from server: " + message.getContent());
    }
}
