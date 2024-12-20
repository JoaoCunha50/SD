package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
            System.out.println(e.getMessage());
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

    public void closeConnection(DataInputStream in, DataOutputStream out) {
        try {
            in.close();
            out.close();
            this.ClientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}