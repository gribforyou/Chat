package ServerPackage;

public class ServerMain {
    public static void main(String[] args){
        final ChatServer chatServer = new ChatServer(args[0], Integer.parseInt(args[1]));
        chatServer.run();
    }
}
