package server;

import common.AuthRequest;
import common.TasksRequest;
import common.User;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class Server {
    private static final int PORT = 12345;
    private int userCounter = 1; // Contador de utilizadores
    private static Semaphore semaforo;
    private final ConcurrentHashMap<Integer, User> userDatabase = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> dataStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Verifica se o argumento foi fornecido
        if (args.length < 1) {
            System.err.println("Erro: É necessário fornecer o número de permissões do semáforo como argumento.");
            System.exit(1);
        }

        try {
            // Lê o número de permissões do semáforo do argumento
            int permits = Integer.parseInt(args[0]);
            semaforo = new Semaphore(permits);
        } catch (NumberFormatException e) {
            System.err.println("Erro: O argumento deve ser um número inteiro.");
            System.exit(1);
        }

        // Instância do servidor para acessar membros não estáticos
        Server server = new Server();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        semaforo.acquire();
                        handleClient(clientSocket);
                    } catch (InterruptedException ie) {
                        System.out.println("Erro no semáforo: " + ie.getMessage());
                    } finally {
                        semaforo.release();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        int clientId = userCounter; // Captura o ID atual do cliente
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

            System.out.println("Received AuthRequest:");
            System.out.println("Username: " + authRequest.getUsername());
            System.out.println("Password: " + authRequest.getPassword());

            User newUser = new User(authRequest.getUsername(), authRequest.getPassword());
            userDatabase.put(clientId, newUser); // Usa o clientId como chave
            System.out.println("User added with ID: " + clientId);
            userCounter++;

            out.writeUTF("User registered successfully!");
            out.flush();
            System.out.println("Sent confirmation to client");

            while (true) {
                try {
                    int taskLength = in.readInt();

                    if (taskLength > 0) {
                        byte[] taskBytes = new byte[taskLength];
                        in.read(taskBytes);

                        TasksRequest task = new TasksRequest();
                        task.readTaskBytes(taskBytes);
                        int type = task.getType();
                        switch (type) {
                            case TasksRequest.PUT -> {
                                dataStorage.put(task.getKey(), task.getValue());
                                System.out.println("Info successfully stored : " + task.toString());
                                out.writeUTF("Info successfully stored!");
                                out.flush();
                            }
                            case TasksRequest.GET -> {
                                String taskResponse = dataStorage.get(task.getKey());
                                if (taskResponse != null) {
                                    System.out.println("Info stored : " + taskResponse);
                                    out.writeInt(taskResponse.getBytes().length);
                                    out.write(taskResponse.getBytes());
                                    out.flush();
                                } else {
                                    System.out.println("No info found, signal the client");
                                    out.writeInt("There is no information associated with the requested key"
                                            .getBytes().length);
                                    out.write("There is no information associated with the requested key".getBytes());
                                    out.flush();
                                }
                            }
                            case TasksRequest.EXIT -> {
                                System.out.println("Client with ID " + clientId + " disconnected.");
                                in.close();
                                out.close();
                                clientSocket.close();
                                return;
                            }
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Client with ID " + clientId + " disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client with ID " + clientId + ": " + e.getMessage());
        }
    }
}
