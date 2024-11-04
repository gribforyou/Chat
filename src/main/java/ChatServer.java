import ClientMessage.AbstractClientMessage;
import ServerMessages.ChatMessage;
import ServerMessages.ResultMessage;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.net.ServerSocket;

public class ChatServer {
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
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + Protocol.PORT);
            System.exit(1);
        }

        try {
            while (true) {
                Socket client = server.accept();
                new ClientHandler(client).start();
            }
        } catch (IOException e) {
        }
    }

    private static class ClientHandler extends Thread {
        private Socket sock;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private boolean isRegistered;
        private String nick;

        public ClientHandler(Socket sock) {
            this.sock = sock;
            this.isRegistered = false;
        }

        private void sendResult(int code){
            try{
                out.writeObject(new ResultMessage(code));
            }
            catch(IOException e){}
        }

        public void run() {
            try {
                in = new ObjectInputStream(sock.getInputStream());
                out = new ObjectOutputStream(sock.getOutputStream());
                //clients.add(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            AbstractClientMessage clientMessage;
            while(!isRegistered && sock.isConnected()) {
                try{
                    clientMessage = (AbstractClientMessage)(in.readObject());
                    if(activeNicks.contains(clientMessage.getContent())){
                        sendResult(Protocol.CONNECTION_FAILURE);
                    }
                    else{
                        sendResult(Protocol.CONNECTION_SUCCESS);
                        nick = clientMessage.getContent();
                        activeNicks.add(nick);
                        clients.add(out);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            ResultMessage resMes;
            while (sock.isConnected()) {
                try{
                    AbstractClientMessage mes = (AbstractClientMessage)(in.readObject());
                    sendResult(Protocol.SENDING_MESSAGE_SUCCESS);
                    ChatMessage chatMessage = new ChatMessage(mes.getContent(), nick, GregorianCalendar.getInstance().getTime());
                    for(ObjectOutputStream o : clients) {
                        o.writeObject(chatMessage);
                    }
                }
                catch (Exception e) {
                    sendResult(Protocol.SENDING_MESSAGE_FAILURE);
                }
            }
        }
    }
}


