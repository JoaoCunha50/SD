package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>Client</code> class represents a client that connects to a server via a TCP
 * socket. It provides methods for sending data, such as putting and getting
 * key-value pairs, and closing the connection with the server.
 * <p>
 * This client communicates with the server over a socket, sending requests to
 * perform tasks like adding (put) and retrieving (get) data. It supports both
 * single and multiple key-value pair operations.
 * </p>
 */
public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    public Socket ClientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Constructs a new `Client` instance and establishes a connection to the
     * server. It creates a socket connection to the specified host and port and
     * initializes input/output streams.
     */
    public Client() {
        try {
            ClientSocket = new Socket(HOST, PORT);
            this.in = new DataInputStream(ClientSocket.getInputStream());
            this.out = new DataOutputStream(ClientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets the host name of the server
     *
     * @return The host name (e.g., "localhost").
     */
    public static String getHost() {
        return HOST;
    }

    /**
     * Gets the port number of the server
     *
     * @return The port number (e.g., 12345).
     */
    public static int getPort() {
        return PORT;
    }

    /**
     * Gets the input stream for receiving data from the server.
     *
     * @return The input stream (`DataInputStream`).
     */
    public DataInputStream getInputStream() {
        return in;
    }

    /**
     * Gets the output stream for sending data to the server.
     *
     * @return The output stream (`DataOutputStream`).
     */
    public DataOutputStream getOutputStream() {
        return out;
    }

    /**
     * Gets the current socket connection to the server.
     *
     * @return The `Socket` object.
     */
    public Socket getSocket() {
        return ClientSocket;
    }

    /**
     * Sets the socket connection to a new client sockets.
     *
     * @param clientSocket The new `Socket` object.
     */
    public void setSocket(Socket clientSocket) {
        ClientSocket = clientSocket;
    }

    /**
     * Sends a "put" request to the server to store a key-value pair.
     *
     * @param key The key to be stored.
     */
    public void put(String key, byte[] value) {
        try {
            out.writeUTF("put");
            out.writeUTF(key);

            out.writeInt(value.length);
            out.write(value);

            System.out.println("Task sent.");
            String response = in.readUTF();
            System.out.println("Response: " + response);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a "get" request to the server to retreive the value for the
     * specified key.
     *
     * @param key The key whose value is to be retrieved..
     * @return A byte array representing the value associated with the key, or
     * null if no data is found.
     */
    public byte[] get(String key) {
        try {
            out.writeUTF("get");
            out.writeUTF(key);

            System.out.println("Task sent.");

            int length = in.readInt();
            byte[] info = null;

            if (length > 0) {
                info = new byte[length];
                in.read(info);
            }

            return info;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Sends a "multiPut" request to store multiple key-value pairs at once.
     *
     * @param pairs A map of keys and their associated byte array values.
     */
    public void multiPut(Map<String, byte[]> pairs) {
        try {
            out.writeUTF("multiPut");
            out.writeInt(pairs.size());

            for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().length);
                out.write(entry.getValue());
            }

            System.out.println("Task sent.");
            String response = in.readUTF();
            System.out.println("Response: " + response);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a "multiGet" request to retrieve the values for multiple keys.
     *
     * @param keys A list of keys whose values are to be retrieved.
     * @return A map of keys and their corresponding byte array values, or null
     * if no data is found.
     */
    public Map<String, byte[]> multiGet(List<String> keys) {
        try {
            out.writeUTF("multiGet");
            out.writeInt(keys.size());

            for (String entry : keys) {
                out.writeUTF(entry);
            }

            System.out.println("Task sent.");
            Map<String, byte[]> responses = new HashMap<>();

            int length = in.readInt();

            for (int i = 0; i < length; i++) {
                String key = in.readUTF();
                int lengthMap = in.readInt();
                byte[] value = new byte[lengthMap];
                in.read(value);
                responses.put(key, value);
            }

            return responses;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Closes the client connection by closing the input/output streams and the
     * socket.
     */
    public void closeConnection() {
        try {
            in.close();
            out.close();
            this.ClientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
