package server;

import common.AuthRequest;
import common.User;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 12345;
    private int userCounter = 1; // Contador de usuários
    private ConcurrentHashMap<Integer, User> userDatabase = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> dataStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Server server = new Server(); // Instância do servidor para acessar membros não estáticos
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Criar uma nova thread para lidar com o cliente
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // Lê o tamanho dos bytes que o cliente vai enviar
            int length = in.readInt();

            // Lê os bytes enviados pelo cliente
            byte[] requestBytes = new byte[length];
            in.read(requestBytes);

            // Converte os bytes para um objeto AuthRequest
            AuthRequest authRequest = new AuthRequest();
            authRequest.readRequestBytes(requestBytes);

            // Imprime os dados recebidos
            System.out.println("Received AuthRequest:");
            System.out.println("Username: " + authRequest.getUsername());
            System.out.println("Password: " + authRequest.getPassword());

            // Cria um novo usuário e armazena no banco de dados
            User newUser = new User(authRequest.getUsername(), authRequest.getPassword());
            userDatabase.put(userCounter, newUser);
            System.out.println("User added with ID: " + userCounter);
            userCounter++;

            // Resposta de confirmação
            out.writeUTF("User registered successfully!");
            out.flush(); // Enviar imediatamente a resposta
            System.out.println("Sent confirmation to client");

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
