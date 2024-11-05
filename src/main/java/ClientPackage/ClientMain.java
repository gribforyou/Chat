package ClientPackage;

public class ClientMain {
    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int port = 8071;

        Client client = new Client(serverIp, port);
        client.start();
    }
}
