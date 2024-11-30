package ClientPackage;

import ProtocolClasses.ClientMessage.ChatClientMessage;
import ProtocolClasses.ClientMessage.RegistrationClientMessage;
import ProtocolClasses.Protocol;
import ProtocolClasses.ServerMessages.AbstractServerMessage;
import ProtocolClasses.ServerMessages.ResultMessage;
import ProtocolClasses.ClientInterface;  // Проверьте, что путь к классу правильный
import ServerPackage.ChatServer;  // Импортируем класс, если он существует в пакете ServerPackage



import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ProtocolClasses.ServerInterface;  // Импорт ServerInterface
import ServerPackage.ServerMain; // Импорт ServerMain
import java.rmi.NotBoundException;  // Импортируем NotBoundException


public class Client {
   /* private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;*/
    private ServerInterface server; // ссылка на сервер

    private String nickName;
    private boolean isConnected;
    private BlockingQueue<ResultMessage> resultQueue;

    public Client(String serverAddress, int port) throws NotBoundException {
        try {
          /*  this.socket = new Socket(serverAddress, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.resultQueue = new LinkedBlockingQueue<>();*/
        	
        	  // Получаем доступ к реестру удаленных объектов
            Registry registry = LocateRegistry.getRegistry(serverAddress, port);
            // Ищем сервер по уникальному имени
            server = (ServerInterface) registry.lookup(ServerMain.UNIQUE_BINDING_NAME);
            this.isConnected = false;
        }  catch (IOException e) {
            System.out.println("Error connecting to the server via RMI: " + e.getMessage());
            System.exit(1);
        }
    }

    public boolean start() {
        if (!register()) {
            System.out.println("Registration failed. Please try again.");
            return false;
        }

        new Thread(new IncomingMessageHandler()).start();
        new Thread(new OutgoingMessageHandler()).start();
        return true;
    }

    
    private boolean register() {
        Scanner scanner = new Scanner(System.in);
        while (!isConnected) {
            System.out.print("Enter your nickname: ");
            this.nickName = scanner.nextLine();

            try {
                /*out.writeObject(new RegistrationClientMessage(nickName));
                ResultMessage response = (ResultMessage) in.readObject();*/

                // Регистрируем клиента на сервере через RMI
             // ClientInterface clientInterface = new ClientHandler(); // создание реализации ClientInterface
                //boolean success = server.registerClient(nickName, clientInterface);
                
                
                if (server.registerClient(nickName, clientInterface)) {
                    System.out.println("You have successfully connected to the chat!");
                    isConnected = true;
                } else /*if (response.getContent().equals(String.valueOf(Protocol.CONNECTION_FAILURE)))*/ {
                    System.out.println("This nickname is already taken. Please try another one.");
                }

            } catch (RemoteException e/*IOException | ClassNotFoundException e*/) {
                System.out.println("Registration error.");
                return false;
            }
        }
        return true;
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                	
                	AbstractServerMessage serverMessage = server.receiveMessage(); // получаем сообщение от сервера
                    System.out.println(serverMessage.getContent());
                    if (serverMessage instanceof ResultMessage) {
                        resultQueue.offer((ResultMessage) serverMessage);
                    } else {
                        System.out.print("\r" + serverMessage.getContent() + "\nEnter message: ");
                    }
                  /*  AbstractServerMessage serverMessage = (AbstractServerMessage) in.readObject();

                    if (serverMessage instanceof ResultMessage) {
                        resultQueue.((ResultMessage) serverMessage);
                    } else {
                        System.out.print("\r" + serverMessage.getContent() + "\nEnter message: ");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading object! Disconnet Server!");
                    try {
                        socket.close();
                    }*/}
                    catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.exit(1);
                }
            }
        }
    
    private class OutgoingMessageHandler implements Runnable {
        private final Scanner scanner = new Scanner(System.in);

        @Override
        public void run() {
            while (true) {
                String messageText = scanner.nextLine();

                ChatClientMessage chatMessage = new ChatClientMessage(messageText, nickName);
                try {
                	 // Отправляем сообщение на сервер через удаленный метод
                    server.sendMessage(chatMessage, nickName);
                    // Ожидаем результат отправки
                      try {
                        ResultMessage result = resultQueue.take();  // Ожидаем результат
                        if (!result.getContent().equals(String.valueOf(Protocol.SENDING_MESSAGE_SUCCESS))) {
                            System.out.println("Message sending error.");
                        }}
                        catch ( InterruptedException  e) {
                            System.err.println("Error sending message! Disconnect Server!");
                        }
                    /*out.writeObject(chatMessage);

                    ResultMessage result = resultQueue.take();
                    if (!result.getContent().equals(String.valueOf(Protocol.SENDING_MESSAGE_SUCCESS))) {
                        System.out.println("Message sending error.");
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message! Disconnect Server!");
                    try {
                        socket.close();
                    } catch (IOException ex) {
                    }
                    System.exit(1);
                } catch (InterruptedException e) {
                    System.err.println("Error waiting for result! Disconnect Server!");
                    try {
                        socket.close();
                     */}
                    catch (RemoteException e /*IOException ex*/) {
                    }
                    System.exit(1);
                }

            }
        }
    }


