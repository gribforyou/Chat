package ClientPackage;

public class ClientMain {
    public static void main(String[] args) {
        String serverIp = "localhost";
        int port = 8071;

        if (args.length >= 1) {
            serverIp = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 8071.");
            }
        }

        Client client = new Client(serverIp, port);
        boolean isConnected = client.start();

        if (isConnected) {
            System.out.println("Connecting to server at " + serverIp + " : " + port);
        } else {
            System.err.println("Failed to connect to server at " + serverIp + " : " + port);
        }
    }
}
