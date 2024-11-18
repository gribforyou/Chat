package ServerPackage;

import ProtocolClasses.ClientMessage.AbstractClientMessage;
import ProtocolClasses.ClientMessage.ChatClientMessage;
import ProtocolClasses.ServerMessages.ChatMessage;
import ProtocolClasses.ServerMessages.ResultMessage;
import ProtocolClasses.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.net.ServerSocket;

public class ChatServer implements Runnable {
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

    private static class ClientHandler extends Thread {
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

        public void run() {
            register();
            if (isRegistered) {
                ChatMessage message = new ChatMessage(nick + " has joined the chat.", "Server", GregorianCalendar.getInstance().getTime());
                sendChatMessage(message);
            }
            while (isRunning) {
                try {
                    Object msg = in.readObject();
                    ChatClientMessage mes = (ChatClientMessage) msg;
                    sendResult(Protocol.SENDING_MESSAGE_SUCCESS);
                    ChatMessage chatMessage = new ChatMessage(mes.getContent(), nick, GregorianCalendar.getInstance().getTime());
                    sendChatMessage(chatMessage);
                } catch (IOException e) {
                    disconnectClients(List.of(this));
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown object is received!");
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                }
            }

        }

        private void sendResult(int code) {
            try {
                out.writeObject(new ResultMessage(code));
            } catch (IOException e) {
                System.err.println("Error sending result!");
                disconnectClients(List.of(this));
            }
        }

        private void sendChatMessage(ChatMessage msg) {
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

        private void disconnectClients(Collection<ClientHandler> toRemove) {
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
            }
        }

        private void register() {
            AbstractClientMessage clientMessage = null;
            while (!isRegistered && sock.isConnected()) {
                try {
                    Object msg = in.readObject();
                    clientMessage = (AbstractClientMessage) (msg);
                    nick = clientMessage.getContent();
                    boolean isOriginalNickname = !activeNicks.contains(nick);
                    if (isOriginalNickname) {
                        activeNicks.add(nick);
                        isRegistered = true;
                        sendResult(Protocol.CONNECTION_SUCCESS);
                    } else {
                        sendResult(Protocol.CONNECTION_FAILURE);
                    }
                } catch (ClassNotFoundException e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                    System.err.println("Read unknown object!");
                } catch (IOException e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                    System.err.println("Error reading object!");
                    disconnectClients(List.of(this));
                }
            }
        }
    }
}


