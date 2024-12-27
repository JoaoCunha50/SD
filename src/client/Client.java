package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    public Socket ClientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client() {
        try {
            ClientSocket = new Socket(HOST, PORT);
            this.in = new DataInputStream(ClientSocket.getInputStream());
            this.out = new DataOutputStream(ClientSocket.getOutputStream());
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

    public DataInputStream getInputStream() {
        return in;
    }

    public DataOutputStream getOutputStream() {
        return out;
    }

    public Socket getSocket() {
        return ClientSocket;
    }

    public void setSocket(Socket clientSocket) {
        ClientSocket = clientSocket;
    }

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