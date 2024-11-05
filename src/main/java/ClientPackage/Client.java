package ClientPackage;

import ProtocolClasses.ClientMessage.ChatClientMessage;
import ProtocolClasses.ClientMessage.RegistrationClientMessage;
import ProtocolClasses.Protocol;
import ProtocolClasses.ServerMessages.AbstractServerMessage;
import ProtocolClasses.ServerMessages.ResultMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nickName;
    private boolean isConnected;

    public Client(String serverAddress, int port) {
        try {
            this.socket = new Socket(serverAddress, port);
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.isConnected = false;
        } catch (IOException e) {
            System.out.println("Error getting streams: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void start() {
        if (!register()) {
            System.out.println("Registration failed. Please try again.");
            return;
        }

        new Thread(new IncomingMessageHandler()).start();
        new Thread(new OutgoingMessageHandler()).start();
    }

    private boolean register() {
        Scanner scanner = new Scanner(System.in);
        while (!isConnected) {
            System.out.print("Enter your nickname: ");
            this.nickName = scanner.nextLine();

            try {
                out.writeObject(new RegistrationClientMessage(nickName));
                ResultMessage response = (ResultMessage) in.readObject();

                if (response.getContent().equals(String.valueOf(Protocol.CONNECTION_SUCCESS))) {
                    System.out.println("You have successfully connected to the chat!");
                    isConnected = true;
                } else {
                    System.out.println("This nickname is already taken, please try another.");
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Registration error.");
                return false;
            }
        }
        return true;
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            while (socket.isConnected()) {
                AbstractServerMessage serverMessage = null;
                try {
                    Object obj = in.readObject();
                    serverMessage = (AbstractServerMessage) (obj);
                } catch (IOException e) {
                    System.err.println("Can't read object!");
                    e.printStackTrace();
                    continue;
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown object is received!");
                    e.printStackTrace();
                    continue;
                }

                if (serverMessage instanceof ResultMessage) {
                    ResultMessage result = (ResultMessage) serverMessage;
                    if (result.getContent().equals(String.valueOf(Protocol.SENDING_MESSAGE_FAILURE))) {
                        System.out.println("Message sending error.");
                    }
                } else {
                    System.out.println(serverMessage.getContent());
                }
            }

        }
    }

    private class OutgoingMessageHandler implements Runnable {
        private final Scanner scanner = new Scanner(System.in);

        @Override
        public void run() {
            while (socket.isConnected()) {
                System.out.print("Enter message: ");
                String messageText = scanner.nextLine();

                ChatClientMessage chatMessage = new ChatClientMessage(messageText, nickName);
                try {
                    out.writeObject(chatMessage);
                } catch (IOException e) {
                    System.err.println("Error sending message!");
                    continue;
                }

                ResultMessage result = null;
                try {
                    Object obj = in.readObject();
                    result = (ResultMessage) (obj);
                } catch (IOException e) {
                    System.err.println("Error receiving message!");
                    System.err.println("Connected: " + socket.isConnected());
                    continue;
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown object received!");
                    System.err.println("Connected: " + socket.isConnected());
                    continue;
                }

                if (result == null) {
                    continue;
                }

                if (!result.getContent().equals(String.valueOf(Protocol.SENDING_MESSAGE_SUCCESS))) {
                    System.out.println("Message sending error.");
                }
            }
        }
    }
}