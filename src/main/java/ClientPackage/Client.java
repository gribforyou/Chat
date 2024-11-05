package ClientPackage;

import ProtocolClasses.ClientMessage.ChatClientMessage;
import ProtocolClasses.ClientMessage.RegistrationClientMessage;
import ProtocolClasses.Protocol;
import ProtocolClasses.ServerMessages.AbstractServerMessage;
import ProtocolClasses.ServerMessages.ResultMessage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nickName;
    private boolean isConnected;
    private BlockingQueue<ResultMessage> resultQueue; 

    public Client(String serverAddress, int port) {
        try {
            this.socket = new Socket(serverAddress, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.isConnected = false;
            this.resultQueue = new LinkedBlockingQueue<>(); 
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
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
                try {
                    AbstractServerMessage serverMessage = (AbstractServerMessage) in.readObject();
                    if (serverMessage instanceof ResultMessage) {
                        resultQueue.offer((ResultMessage) serverMessage);
                    } else {
                        System.out.println(serverMessage.getContent());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading object!");
                    e.printStackTrace();
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

                    ResultMessage result = resultQueue.take();
                    if (!result.getContent().equals(String.valueOf(Protocol.SENDING_MESSAGE_SUCCESS))) {
                        System.out.println("Message sending error.");
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message!");
                } catch (InterruptedException e) {
                    System.err.println("Error waiting for result!");
                }
            }
        }
    }
}
