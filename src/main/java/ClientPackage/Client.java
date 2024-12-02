package ClientPackage;

import ServerPackage.MessageSender;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client implements MessageVisualiser, Runnable {
    private final String SERVER_NAME;
    private final int SERVER_PORT;
    private final int CLIENT_PORT;
    private Registry registry;
    private final Scanner scanner = new Scanner(System.in);
    private MessageSender server;
    private String nickname;

    public Client(String serverName, int serverPort, int clientPort) {
        this.SERVER_NAME = serverName;
        this.SERVER_PORT = serverPort;
        this.CLIENT_PORT = clientPort;
        try {
            registry = LocateRegistry.getRegistry(SERVER_PORT);
            server = (MessageSender) registry.lookup(SERVER_NAME);
        }
        catch (Exception e) {
            System.err.println("Can't connect to the server");
            System.exit(1);
        }

        try {
            register();
        } catch (RemoteException e) {
            System.err.println("Can't register");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Enter message:");
            String message = scanner.nextLine();
            try {
                server.sendChatMessage(nickname, message);
            } catch (RemoteException e) {
                System.err.println("Server is disconnected!");
                System.exit(1);
            }
        }
    }

    private void register() throws RemoteException {
        boolean isRegistered = false;
        while (!isRegistered) {
            System.out.println("Enter your nick:");
            nickname = scanner.nextLine();
            Remote stub = UnicastRemoteObject.exportObject(this, CLIENT_PORT);
            try {
                registry.bind(nickname, stub);
                isRegistered = server.registerNick(nickname);
            } catch (AlreadyBoundException e) {
                System.out.println("This nick is already registered! Please enter a new nick:");
            }
        }
        System.out.println("You have been registered successfully!");
    }

    @Override
    public void visualiseMessage(String sender, String message) throws RemoteException {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

        System.out.println("[" + currentDate + " " + currentTime + "] " + sender + ": " + message);
    }
}
