import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.net.ServerSocket;

public class ChatServer {
    private static List<PrintWriter> clients;
    private static ServerSocket server;

    public ChatServer() {
        clients = new ArrayList<PrintWriter>();
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
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket sock) {
            this.sock = sock;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(sock.getOutputStream(), true);
                clients.add(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


