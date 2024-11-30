package ServerPackage;

import ProtocolClasses.ServerMessages.ChatMessage;
import ProtocolClasses.Protocol;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;
import java.net.ServerSocket;

public class ChatServer implements Runnable, MessageSender {
    private static List<ClientHandler> clientHandlers;
    private static Set<String> activeNicks;
    private static ServerSocket server;

    public ChatServer() {
        clientHandlers = new ArrayList<ClientHandler>();
        activeNicks = new HashSet<String>();
    }

    public void run() {
        try {
            server = new ServerSocket(Protocol.PORT);
            System.err.println("Server started at " + server.getInetAddress());
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + Protocol.PORT);
            System.exit(1);
        }


        while (true) {
            try {
                Socket client = server.accept();
                System.err.println("New client connected: " + client.getInetAddress());
                ClientHandler handler = new ClientHandler(client);
                clientHandlers.add(handler);
                handler.start();
            } catch (IOException e) {
                System.err.println("Accept failed!");
            }
        }
    }

    @Override
    public void sendChatMessage(ChatMessage msg) throws RemoteException {
        List<ClientHandler> toRemove = new ArrayList<ClientHandler>();
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                try {
                    handler.out.writeObject(msg);
                } catch (IOException e) {
                    toRemove.add(handler);
                }
            }
        }
        disconnectClients(toRemove);
    }

    @Override
    public boolean registerNick(String nick) throws RemoteException {
        synchronized (activeNicks) {
            if (activeNicks.contains(nick)) {
                return false;
            }
            else {
                activeNicks.add(nick);
                return true;
            }
        }
    }

    public void disconnectClients(Collection<ClientHandler> toRemove) throws RemoteException {
        for (ClientHandler handler : toRemove) {
            try {
                handler.isRunning = false;
                handler.sock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            synchronized (activeNicks) {
                activeNicks.remove(handler.nick);
            }
            synchronized (clientHandlers) {
                clientHandlers.remove(handler);
            }
            ChatMessage message = new ChatMessage(handler.nick + " is left", "Server", GregorianCalendar.getInstance().getTime());
            sendChatMessage(message);
            System.err.println("Client disconnected: " + handler.sock.getInetAddress());
        }
    }

    private static class ClientHandler extends Thread  {
        private Socket sock;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String nick;
        boolean isRunning = true;
        private boolean isRegistered;

        public ClientHandler(Socket sock) throws IOException {
            this.sock = sock;
            this.isRegistered = false;
            try {
                in = new ObjectInputStream(sock.getInputStream());
                out = new ObjectOutputStream(sock.getOutputStream());
            } catch (IOException e) {
                System.err.println("Could not open input and output streams!");
                throw new IOException("Can't open streams!");
            }
        }
    }
}