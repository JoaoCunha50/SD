package server;

import common.AuthRequest;
import common.User;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Server implements Serializable {
    private static final int PORT = 12345;
    private static Semaphore semaforo;
    private final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[]> dataStorage = new ConcurrentHashMap<>();
    private final List<Socket> clientConnections = new ArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static final String USER_DB_FILE = "Data/userDatabase.obj";
    private static final String DATA_STORAGE_FILE = "Data/dataStorage.obj";

    private boolean running = true; // Controle do loop do servidor

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Erro: É necessário fornecer o número de permissões do semáforo como argumento.");
            System.exit(1);
        }

        try {
            int permits = Integer.parseInt(args[0]);
            semaforo = new Semaphore(permits);
        } catch (NumberFormatException e) {
            System.err.println("Erro: O argumento deve ser um número inteiro.");
            System.exit(1);
        }

        Server server = new Server();

        server.loadState();

        // Adiciona um hook para salvar o estado e fechar conexões
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nGracefully shutting down the server...");
            server.gracefulShutdown();
        }));

        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    clientConnections.add(clientSocket);

                    threadPool.submit(() -> { // Submete a tarefa ao pool de threads
                        try {
                            semaforo.acquire();
                            handleClient(clientSocket);
                        } catch (InterruptedException ie) {
                            System.out.println("Erro no semáforo: " + ie.getMessage());
                            Thread.currentThread().interrupt(); // Reinterrompe a thread
                        } finally {
                            semaforo.release();
                            clientConnections.remove(clientSocket); // Remove a conexão ao encerrar
                        }
                    });
                } catch (SocketException e) {
                    if (running) {
                        System.out.println("Erro no servidor: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            User user = new User();

            int flag = 0;
            while (flag == 0) {
                // Lê o tamanho dos bytes que o cliente vai enviar
                int length = in.readInt();

                // Lê os bytes enviados pelo cliente
                byte[] requestBytes = new byte[length];
                in.read(requestBytes);

                // Converte os bytes para um objeto AuthRequest
                AuthRequest authRequest = new AuthRequest();
                authRequest.readRequestBytes(requestBytes);
                user = new User(authRequest.getUsername(), authRequest.getPassword());

                int requestType = authRequest.getType();

                switch (requestType) {
                    case AuthRequest.REGISTER -> {
                        int success = user.registerAuth(userDatabase);
                        if (success == 1) {
                            System.out.println("User added with Username: " + user.getUsername());

                            out.writeUTF("User registered successfully!");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();

                            flag = 1;
                        } else {

                            out.writeUTF("There is already a user with such credentials.");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();
                        }
                    }
                    case AuthRequest.LOGIN -> {
                        int success = user.loginAuth(userDatabase);
                        if (success == 1) {

                            out.writeUTF("User logged in successfully!");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();

                            flag = 1;
                        } else if (success == -1) {

                            out.writeUTF("Password is invalid!");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();
                        } else {

                            out.writeUTF("There is no user with such credentials.");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();
                        }
                    }
                }

            }

            while (true) {
                try {
                    String taskType = in.readUTF();

                    switch (taskType) {
                        case "put" -> {
                            String key = in.readUTF();
                            int length = in.readInt();
                            byte[] value = new byte[length];
                            in.read(value);

                            dataStorage.put(key, value);
                            System.out.println(
                                    "Info successfully stored -> Key: " + key + " | Value: " + new String(value));
                            out.writeUTF("Info successfully stored!");
                            out.flush();
                        }
                        case "multiPut" -> {
                            int N = in.readInt();

                            for (int i = 0; i < N; i++) {
                                String key = in.readUTF();
                                int length = in.readInt();
                                byte[] value = new byte[length];
                                in.read(value);

                                System.out.println(
                                        "Info successfully stored -> Key: " + key + " | Value: " + new String(value)
                                                + "\n");
                                dataStorage.put(key, value);
                            }

                            out.writeUTF("Info successfully stored!");
                            out.flush();
                        }
                        case "get" -> {
                            String key = in.readUTF();
                            byte[] taskResponse = dataStorage.get(key);
                            if (taskResponse != null) {
                                System.out.println("Info stored : " + taskResponse);
                                out.writeInt(taskResponse.length);
                                out.write(taskResponse);
                                out.flush();
                            } else {
                                System.out.println("No info found, signal the client");
                                out.writeUTF(
                                        "There is no information associated with the requested key ( " + key + " )");
                                out.flush();
                            }
                        }
                        case "multiGet" -> {
                            HashMap<String, byte[]> pairs = new HashMap<>();

                            int N = in.readInt();
                            for (int i = 0; i < N; i++) {
                                String key = in.readUTF();
                                byte[] value = dataStorage.get(key);
                                if (value != null) {
                                    pairs.put(key, value);
                                } else
                                    System.out.println("There is no value associated with '" + key + "'\n");
                            }

                            if (pairs.size() > 0) {
                                out.writeInt(pairs.size());

                                for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                                    out.writeUTF(entry.getKey());
                                    out.writeInt(entry.getValue().length);
                                    out.write(entry.getValue());
                                    out.flush();
                                }
                            } else {
                                System.out.println("No info found, signal the client");
                                out.writeUTF("There is no information associated with the requested keys");
                                out.flush();
                            }
                        }
                        case "exit" -> {
                            System.out.println("Client with username " + user.getUsername() + " disconnected.");
                            closeConnection(in, out, clientSocket);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Client with username " + user.getUsername() + " disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private void gracefulShutdown() {

        saveState();

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of remaining threads...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        for (Socket socket : clientConnections) {
            try {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }

        System.out.println("All client connections and threads closed. Server shutdown completed.");
        running = false; // Finaliza o loop principal do servidor
    }

    public void closeConnection(DataInputStream in, DataOutputStream out, Socket socket) {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void saveState() {
        try (ObjectOutputStream userOut = new ObjectOutputStream(new FileOutputStream(USER_DB_FILE));
                ObjectOutputStream dataOut = new ObjectOutputStream(new FileOutputStream(DATA_STORAGE_FILE))) {

            userOut.writeObject(userDatabase);
            dataOut.writeObject(dataStorage);
        } catch (IOException e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }

    private void loadState() {
        try (ObjectInputStream userIn = new ObjectInputStream(new FileInputStream(USER_DB_FILE));
                ObjectInputStream dataIn = new ObjectInputStream(new FileInputStream(DATA_STORAGE_FILE))) {

            Object loadedUserObject = userIn.readObject();
            Object loadedDataObject = dataIn.readObject();

            if (loadedUserObject instanceof ConcurrentHashMap<?, ?> usersMap) {
                usersMap.forEach((key, value) -> {
                    if (key instanceof String && value instanceof User) {
                        userDatabase.put((String) key, (User) value);
                    }
                });
            } else {
                System.err.println("Error loading userDatabase: Incompatible type.");
            }

            if (loadedDataObject instanceof ConcurrentHashMap<?, ?> dataMap) {
                dataMap.forEach((key, value) -> {
                    if (key instanceof String && value instanceof byte[]) {
                        dataStorage.put((String) key, (byte[]) value);
                    }
                });
            } else {
                System.err.println("Error loading dataStorage: Incompatible type.");
            }

            System.out.println("State successfully loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous state found. Starting with empty maps");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading state: " + e.getMessage());
        }
    }
}
