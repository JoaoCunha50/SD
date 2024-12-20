package client;

import common.AuthRequest;
import java.io.*;
import java.util.Scanner;

public class ClientInterface {

    public static void main(String[] args) {
        Client client = new Client();

        try (
                DataInputStream in = new DataInputStream(client.getSocket().getInputStream());
                DataOutputStream out = new DataOutputStream(client.getSocket().getOutputStream());
                Scanner scanner = new Scanner(System.in)) {

            int flag = 0;
            String command;

            // Authentication loop
            while (flag == 0) {
                System.out.print("Enter command (register/login): ");
                command = scanner.nextLine();
                switch (command) {
                    case "register":
                        System.out.print("Username: ");
                        String regUsername = scanner.nextLine();
                        System.out.print("Password: ");
                        String regPassword = scanner.nextLine();
                        AuthRequest registerRequest = new AuthRequest(regUsername, regPassword);
                        byte[] regRequestBytes = registerRequest.getRequestBytes();
                        out.writeInt(regRequestBytes.length);
                        out.write(regRequestBytes);
                        flag = 1;
                        out.flush();
                        break;
                    case "login":
                        System.out.print("Username: ");
                        String loginUsername = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginPassword = scanner.nextLine();
                        AuthRequest loginRequest = new AuthRequest(loginUsername, loginPassword);
                        byte[] loginRequestBytes = loginRequest.getRequestBytes();
                        out.writeInt(loginRequestBytes.length);
                        out.write(loginRequestBytes);
                        flag = 1;
                        out.flush();
                        break;
                    default:
                        System.out.println("Unknown command. Please enter 'register' or 'login'.");
                }
            }

            // Interaction loop
            while (true) {
                System.out.print("Enter command (put/get/exit): ");
                command = scanner.nextLine();
                switch (command) {
                    case "put":
                        System.out.print("Key: ");
                        String putKey = scanner.nextLine();
                        System.out.print("Value: ");
                        String value = scanner.nextLine();
                        out.writeUTF("put " + putKey + " " + value);
                        System.out.println("Put command sent.");
                        break;
                    case "get":
                        System.out.print("Key: ");
                        String getKey = scanner.nextLine();
                        out.writeUTF("get " + getKey);
                        String response = in.readUTF();
                        System.out.println("Response: " + response);
                        break;
                    case "exit":
                        System.out.println("Exiting...");
                        out.writeUTF("exit"); // Envie o comando exit
                        out.flush();
                        client.getSocket().close(); // Agora feche o socket
                        return;
                    default:
                        System.out.println("Unknown command. Please enter 'put', 'get', or 'exit'.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
