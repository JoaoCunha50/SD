package server;

import common.AuthRequest;
import common.TasksRequest;
import common.User;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class Server implements Serializable {
    private static final int PORT = 12345;
    private static Semaphore semaforo;
    private final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> dataStorage = new ConcurrentHashMap<>();

    private static final String USER_DB_FILE = "Data/userDatabase.obj";
    private static final String DATA_STORAGE_FILE = "Data/dataStorage.obj";

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

        Server server = new Server();

        server.loadState();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing Server. Saving current state...");
            server.saveState();
            System.out.println("State successfully saved.");
        }));

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

                            out.writeUTF("There is already a User with such credentials.");
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

                            out.writeUTF("There is no User with such credentials.");
                            out.flush();
                            System.out.println("Sent notification to client");
                            System.out.println();
                        }
                    }
                }

            }

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
                                System.out.println("Client with username " + user.getUsername() + " disconnected.");
                                in.close();
                                out.close();
                                clientSocket.close();
                                return;
                            }
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
                    if (key instanceof String && value instanceof String) {
                        dataStorage.put((String) key, (String) value);
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
