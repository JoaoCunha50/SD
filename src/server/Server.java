package server;

import common.AuthRequest;
import common.User;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The <code>Server</code> class is responsible for handling client connections,
 * authentication, and data storage. It uses a semaphore to control the number
 * of concurrent connections and a thread pool to handle client requests.
 */
public class Server implements Serializable {

    /**
     * The port number for the server to listen on.
     */
    private static final int PORT = 12345;

    /**
     * Semaphore to limit the number of concurrent client connections.
     */
    private static Semaphore semaforo;

    /**
     * The map that stores user information, with the username as the key.
     */
    private final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();

    /**
     * The map that stores data associated with keys, using a String key and
     * byte array value.
     */
    private final ConcurrentHashMap<String, byte[]> dataStorage = new ConcurrentHashMap<>();

    /**
     * List to keep track of active client connections.
     */
    private final List<Socket> clientConnections = new ArrayList<>();

    /**
     * Thread pool to handle client requests concurrently.
     */
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * File paths for storing the user database and data storage.
     */
    private static final String USER_DB_FILE = "Data/userDatabase.obj";
    private static final String DATA_STORAGE_FILE = "Data/dataStorage.obj";

    /**
     * Flag to control the server's running state.
     */
    private boolean running = true;

    /**
     * The main method that initializes the server, sets up the semaphore, and
     * starts listening for client connections. It also adds a shutdown hook to
     * gracefully close connections and save the server state when the server is
     * stopped.
     *
     * @param args Command-line arguments. The first argument should be the
     *             number of semaphore permits.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(
                    "\u001B[31m[ERROR]\u001B[0m É necessário fornecer o número de permissões do semáforo como argumento.");
            System.exit(1);
        }

        try {
            int permits = Integer.parseInt(args[0]);
            semaforo = new Semaphore(permits);
        } catch (NumberFormatException e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m O argumento deve ser um número inteiro.");
            System.exit(1);
        }

        Server server = new Server();

        server.loadState();

        // Add shutdown hook to save state and close connections gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\u001B[33m[SHUTDOWN]\u001B[0m Gracefully shutting down the server...");
            server.gracefulShutdown();
        }));

        server.start();
    }

    /**
     * Starts the server, listening for client connections and spawning a new
     * thread for each connection. Each thread will handle a client request
     * while the semaphore ensures that only a specified number of clients are
     * processed concurrently.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("\u001B[32m[SERVER]\u001B[0m Server is running on port " + PORT);

            while (running) {
                try {
                    // Accept a new client connection
                    Socket clientSocket = serverSocket.accept();
                    clientConnections.add(clientSocket);

                    // Submit a new task to handle the client in a separate thread
                    threadPool.submit(() -> {
                        try {
                            semaforo.acquire(); // Acquire semaphore permit for client
                            handleClient(clientSocket);
                        } catch (InterruptedException ie) {
                            System.out.println("\u001B[31m[ERROR]\u001B[0m Erro no semáforo: " + ie.getMessage());
                            Thread.currentThread().interrupt(); // Re-interrupt the thread
                        } finally {
                            semaforo.release(); // Release the semaphore permit
                            clientConnections.remove(clientSocket); // Remove the client connection
                        }
                    });
                } catch (SocketException e) {
                    if (running) {
                        System.out.println("\u001B[31m[ERROR]\u001B[0m Erro no servidor: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("\u001B[31m[ERROR]\u001B[0m Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    /**
     * Handles a client connection by reading authentication requests,
     * processing data storage operations, and responding with the appropriate
     * messages based on the task.
     *
     * @param clientSocket The socket representing the client connection.
     */
    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            User user = new User();
            int flag = 0;
            while (flag == 0) {
                // Read the length of the incoming request and the request data itself
                int length = in.readInt();
                byte[] requestBytes = new byte[length];
                in.read(requestBytes);

                // Deserialize the request data into an AuthRequest object
                AuthRequest authRequest = new AuthRequest();
                authRequest.readRequestBytes(requestBytes);
                user = new User(authRequest.getUsername(), authRequest.getPassword());

                // Authenticate the user based on the request type (REGISTER or LOGIN)
                int requestType = authRequest.getType();
                int success;
                switch (requestType) {
                    case AuthRequest.REGISTER -> {
                        // Handle user registration
                        try {
                            lock.lock();
                            success = user.registerAuth(userDatabase);
                        } finally {
                            lock.unlock();
                        }
                        if (success == 1) {
                            System.out.println(
                                    "\u001B[32m[AUTH]\u001B[0m User added with Username: " + user.getUsername());
                            out.writeInt(1);
                            out.writeUTF("User registered successfully!");
                            out.flush();
                            System.out.println("\u001B[36m[INFO]\u001B[0m Sent notification to client");
                            System.out.println();
                            flag = 1;
                        } else {
                            out.writeInt(0);
                            out.writeUTF("There is already a user with such credentials.");
                            out.flush();
                            System.out.println("\u001B[36m[INFO]\u001B[0m Sent notification to client");
                            System.out.println();
                        }
                    }
                    case AuthRequest.LOGIN -> {
                        // Handle user login
                        try {
                            lock.lock();
                            success = user.loginAuth(userDatabase);
                        } finally {
                            lock.unlock();
                        }
                        if (success == 1) {
                            out.writeInt(1);
                            out.writeUTF("User logged in successfully!");
                            out.flush();
                            System.out.println("\u001B[36m[INFO]\u001B[0m Sent notification to client");
                            System.out.println();
                            flag = 1;
                        } else if (success == -1) {
                            out.writeInt(0);
                            out.writeUTF("Password is invalid!");
                            out.flush();
                            System.out.println("\u001B[36m[INFO]\u001B[0m Sent notification to client");
                            System.out.println();
                        } else {
                            out.writeInt(0);
                            out.writeUTF("There is no user with such credentials.");
                            out.flush();
                            System.out.println("\u001B[36m[INFO]\u001B[0m Sent notification to client");
                            System.out.println();
                        }
                    }
                }
            }

            // Continue to process data storage tasks (put, get, multiPut, multiGet)
            while (true) {
                String taskType = in.readUTF();
                switch (taskType) {
                    case "put" -> {
                        String key = in.readUTF();
                        int length = in.readInt();
                        byte[] value = new byte[length];
                        in.read(value);

                        lock.lock();
                        try {
                            if (!dataStorage.containsKey(key)) {
                                dataStorage.put(key, value);
                            } else {
                                System.out
                                        .println("\u001B[33m[WARNING]\u001B[0m There is already a key with that value\n");
                                out.writeUTF("There is already a key with that name!\n");
                                break;
                            }
                        } finally {
                            condition.signalAll();
                            lock.unlock();
                        }
                        System.out.println(
                                "\u001B[32m[DATA]\u001B[0m Info successfully stored -> Key: " + key + " | Value: "
                                        + new String(value));
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
                                    "\u001B[32m[DATA]\u001B[0m Info successfully stored -> Key: " + key + " | Value: "
                                            + new String(value)
                                            + "\n");
                            lock.lock();
                            try {
                                if (!dataStorage.containsKey(key)) {
                                    dataStorage.put(key, value);
                                } else {
                                    System.out.println(
                                            "\u001B[33m[WARNING]\u001B[0m There is already a key with that name, notifiyng client\n");
                                    out.writeUTF("There is already a key with that name!\n");
                                }
                            } finally {
                                condition.signalAll();
                                lock.unlock();
                            }
                        }

                        out.writeUTF("Info successfully stored!");
                        out.flush();
                    }
                    case "get" -> {
                        String key = in.readUTF();
                        byte[] taskResponse = null;
                        lock.lock();
                        try {
                            taskResponse = dataStorage.get(key);
                        } finally {
                            lock.unlock();
                        }
                        if (taskResponse != null) {
                            System.out.println("\u001B[32m[DATA]\u001B[0m Info stored : " + new String(taskResponse));
                            out.writeInt(taskResponse.length);
                            out.write(taskResponse);
                            out.flush();
                        } else {
                            System.out.println("\u001B[33m[WARNING]\u001B[0m No info found, signal the client");
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
                                lock.lock();
                                try {
                                    pairs.put(key, value);
                                } finally {
                                    lock.lock();
                                }
                            } else {
                                System.out.println(
                                        "\u001B[33m[WARNING]\u001B[0m There is no value associated with '" + key + "'\n");
                            }
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
                            System.out.println("\u001B[33m[WARNING]\u001B[0m No info found, signal the client");
                            out.writeUTF("There is no information associated with the requested keys");
                            out.flush();
                        }
                    }
                    case "getWhen" -> {
                        String key = in.readUTF();
                        String keyCond = in.readUTF();
                        int length = in.readInt();
                        byte[] valueCond = new byte[length];
                        in.read(valueCond);

                        lock.lock();
                        try {
                            while (!isConditionSatisfied(keyCond, valueCond)) {
                                condition.await();
                            }
                            byte[] taskResponse = dataStorage.get(key);
                            if (taskResponse != null) {
                                System.out
                                        .println("\u001B[32m[DATA]\u001B[0m Info stored : " + new String(taskResponse));
                                out.writeInt(taskResponse.length);
                                out.write(taskResponse);
                                out.flush();
                            } else {
                                System.out.println("\u001B[33m[WARNING]\u001B[0m No info found, signal the client");
                                out.writeUTF(
                                        "There is no information associated with the requested key ( " + key + " )");
                                out.flush();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            out.writeInt(0);
                        } finally {
                            lock.unlock();
                        }
                    }
                    case "exit" -> {
                        System.out.println("\u001B[36m[INFO]\u001B[0m Client with username " + user.getUsername()
                                + " disconnected.");
                        closeConnection(in, out, clientSocket);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("\u001B[31m[ERROR]\u001B[0m Error handling client: " + e.getMessage());
        }
    }

    private boolean isConditionSatisfied(String keyCond, byte[] valueCond) {
        byte[] value = dataStorage.get(keyCond);
        if (value == null) {
            return false;
        }
        return java.util.Arrays.equals(value, valueCond);
    }

    /**
     * Gracefully shuts down the server, saving the state, shutting down the
     * thread pool, and closing all active client connections.
     */
    private void gracefulShutdown() {
        saveState();
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("\u001B[33m[SHUTDOWN]\u001B[0m Forcing shutdown of remaining threads...");
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
                System.err.println("\u001B[31m[ERROR]\u001B[0m Error closing client connection: " + e.getMessage());
            }
        }

        System.out.println(
                "\u001B[32m[SERVER]\u001B[0m All client connections and threads closed. Server shutdown completed.");
        running = false;
    }

    /**
     * Closes the given client connection by closing its input/output streams
     * and the socket itself.
     *
     * @param in     The input stream for the client.
     * @param out    The output stream for the client.
     * @param socket The socket representing the client connection.
     */
    private void closeConnection(DataInputStream in, DataOutputStream out, Socket socket) {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("\u001B[31m[ERROR]\u001B[0m " + e.getMessage());
        }
    }

    /**
     * Saves the current state of the server, including the user database and
     * data storage, to files.
     */
    private void saveState() {
        try (ObjectOutputStream userOut = new ObjectOutputStream(new FileOutputStream(USER_DB_FILE));
                ObjectOutputStream dataOut = new ObjectOutputStream(new FileOutputStream(DATA_STORAGE_FILE))) {

            userOut.writeObject(userDatabase);
            dataOut.writeObject(dataStorage);
        } catch (IOException e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m Error saving state: " + e.getMessage());
        }
    }

    /**
     * Loads the saved state of the server, including the user database and data
     * storage, from files.
     */
    private void loadState() {
        try (ObjectInputStream userIn = new ObjectInputStream(new FileInputStream(USER_DB_FILE));
                ObjectInputStream dataIn = new ObjectInputStream(new FileInputStream(DATA_STORAGE_FILE))) {

            userDatabase.putAll((Map<String, User>) userIn.readObject());
            dataStorage.putAll((Map<String, byte[]>) dataIn.readObject());
            System.out.println("\u001B[32m[STATE]\u001B[0m State successfully loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("\u001B[31m[STATE]\u001B[0m No previous state found. Starting with empty maps.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("\u001B[31m[STATE]\u001B[0m Error loading state: " + e.getMessage());
        }
    }

}