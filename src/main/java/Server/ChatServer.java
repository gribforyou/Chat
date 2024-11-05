package Server;

import ProtocolClasses.ClientMessage.AbstractClientMessage;
import ProtocolClasses.ServerMessages.ChatMessage;
import ProtocolClasses.ServerMessages.ResultMessage;
import ProtocolClasses.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.net.ServerSocket;

public class ChatServer implements Runnable {
    private static List<ObjectOutputStream> clients;
    private static Set<String> activeNicks;
    private static ServerSocket server;

    public ChatServer() {
        clients = new ArrayList<ObjectOutputStream>();
        activeNicks = new HashSet<String>();
    }

    public void run() {
        try {
            server = new ServerSocket(Protocol.PORT);
            System.err.println("Server started");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + Protocol.PORT);
            System.exit(1);
        }


        while (true) {
            try{
                Socket client = server.accept();
                new ClientHandler(client).start();
            }
            catch (IOException e) {
                System.err.println("Accept failed!");
            }
        }
    }

    private static class ClientHandler extends Thread{
        private Socket sock;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String nick;
        private boolean isRegistered;

        public ClientHandler(Socket sock) throws IOException {
            this.sock = sock;
            this.isRegistered = false;
            try{
                in = new ObjectInputStream(sock.getInputStream());
                out = new ObjectOutputStream(sock.getOutputStream());
            }
            catch (IOException e) {
                System.err.println("Could not open input and output streams!");
                throw new IOException("Can't open streams!");
            }
        }

        public void run() {
            register();
            ResultMessage resMes;
            while (sock.isConnected()) {
                try {
                    AbstractClientMessage mes = (AbstractClientMessage) (in.readObject());
                    sendResult(Protocol.SENDING_MESSAGE_SUCCESS);
                    ChatMessage chatMessage = new ChatMessage(mes.getContent(), nick, GregorianCalendar.getInstance().getTime());
                    for (ObjectOutputStream o : clients) {
                        o.writeObject(chatMessage);
                    }
                } catch (Exception e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                }
            }
        }

        private void sendResult(int code) {
            try {
                out.writeObject(new ResultMessage(code));
            }
            catch (IOException e) {
                System.err.println("Error sending result!");
            }
        }

        private void register() {
            AbstractClientMessage clientMessage = null;
            while (!isRegistered && sock.isConnected()) {
                try {
                    clientMessage = (AbstractClientMessage) (in.readObject());
                    nick = clientMessage.getContent();
                    boolean isOriginalNickname = !activeNicks.contains(nick);
                    if (isOriginalNickname) {
                        clients.add(out);
                        activeNicks.add(nick);
                        isRegistered = true;
                        sendResult(Protocol.SENDING_MESSAGE_SUCCESS);
                    }
                }
                catch (ClassNotFoundException e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                    System.err.println("Read unknown object!");
                }
                catch (IOException e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                    System.err.println("Error reading object!");
                }
            }
        }
    }
}


