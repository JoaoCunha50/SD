package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import common.User;

public class Server {
    private static final int PORT = 12345;
    private final int userCounter = 1;
    private static ConcurrentHashMap<Integer, User> userDatabase = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, byte[]> dataStore = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
