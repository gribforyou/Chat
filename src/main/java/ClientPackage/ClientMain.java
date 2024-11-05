package ClientPackage;

public class ClientMain {
    public static void main(String[] args) {
        String serverIp = "151.249.190.191"; 
        int port = 8071;

        Client client = new Client(serverIp, port);
        client.start();
    }
}
