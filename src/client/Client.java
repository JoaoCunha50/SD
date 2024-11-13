package client;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    public Socket ClientSocket;

    public Client() {
        try {
            ClientSocket = new Socket(HOST, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPort() {
        return PORT;
    }

    public Socket getSocket() {
        return ClientSocket;
    }

    public void setSocket(Socket clientSocket) {
        ClientSocket = clientSocket;
    }
}