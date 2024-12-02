package ClientPackage;

import ServerPackage.MessageSender;

import java.net.Inet4Address;
import java.net.UnknownHostException;
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
    private final String SERVER_IP;
    private final  String CLIENT_IP;
    private final int CLIENT_PORT;
    private Registry registry;
    private final Scanner scanner = new Scanner(System.in);
    private MessageSender server;
    private String nickname;

    public Client(String serverName, String serverIp, int serverPort, int clientPort) throws UnknownHostException {
        this.SERVER_NAME = serverName;
        this.SERVER_IP = serverIp;
        this.SERVER_PORT = serverPort;
        this.CLIENT_PORT = clientPort;
        this.CLIENT_IP = String.valueOf(Inet4Address.getLocalHost()).split("/")[1];
        System.err.println(CLIENT_IP);
        try {
            registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
            server = (MessageSender) registry.lookup(SERVER_NAME);
        } catch (Exception e) {
            System.err.println("Can't connect to the server");
            System.exit(1);
        }

        try {
            register();
        } catch (RemoteException e) {
            System.err.println("Can't register");
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
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

    private void register() throws RemoteException, AlreadyBoundException {
        boolean isRegistered = false;
        while (!isRegistered) {
            System.out.println("Enter your nick:");
            nickname = scanner.nextLine();
            Remote stub = UnicastRemoteObject.exportObject(this, CLIENT_PORT);
            Registry tempRegistry = LocateRegistry.createRegistry(CLIENT_PORT);
            tempRegistry.rebind(nickname, stub);
            isRegistered = server.registerNick(nickname, CLIENT_IP, CLIENT_PORT);
            System.err.println("Run method register in server");
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
