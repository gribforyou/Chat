package ServerPackage;

import ClientPackage.MessageVisualiser;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ChatServer implements MessageSender, Runnable {
    private final String SERVER_NAME;
    private final int SERVER_PORT;
    private Registry registry;
    private Map<String, MessageVisualiser> clients;

    public ChatServer(String serverName, int port){
        SERVER_NAME = serverName;
        SERVER_PORT = port;
        try{
            registry = LocateRegistry.createRegistry(SERVER_PORT);
        }
        catch(RemoteException e){
            System.err.println("Error with connection or port!");
            System.exit(1);
        }
        clients = new HashMap<>();
    }

    @Override
    public void run(){
        try{
            Remote stub = UnicastRemoteObject.exportObject(this, SERVER_PORT);
            registry.bind(SERVER_NAME, stub);
        } catch (Exception e) {
            System.err.println("Can't create stub!");
            throw new RuntimeException(e);
        }
        System.err.println("Server started");
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendChatMessage(String sender, String message) {
        List<String> toRemove = new ArrayList<>();
        synchronized (clients) {
            for(var client : clients.keySet()) {
                try{
                    clients.get(client).visualiseMessage(sender, message);
                }
                catch(RemoteException e){
                    toRemove.add(client);
                }
            }
        }
        disconnectClients(toRemove);
    }

    @Override
    public boolean registerNick(String nick){
        synchronized (clients) {
            if (clients.containsKey(nick)) {
                return false;
            }
            else {
                try{
                    MessageVisualiser client = (MessageVisualiser) registry.lookup(nick);
                    clients.put(nick, client);
                    System.err.println(nick + " has been registered!");
                    sendChatMessage("Server", nick + " has joined the chat");
                    return true;
                }
                catch (NotBoundException | RemoteException e) {
                    System.err.println("Error connecting to client");
                    return false;
                }
            }
        }
    }

    public void disconnectClients(Collection<String> toRemove) {
        for (var client : toRemove) {
            clients.remove(client);
            System.err.println(client + " has been disconnected!");
            sendChatMessage("Server", client + " disconnected");
        }
    }
}
