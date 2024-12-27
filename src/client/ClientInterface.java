package client;

import common.AuthRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientInterface {

    public static void main(String[] args) {
        Client client = new Client();

        try (
                DataInputStream in = client.getInputStream();
                DataOutputStream out = client.getOutputStream();
                Scanner scanner = new Scanner(System.in)) {

            int flag = 0;
            String command;

            // Authentication loop
            while (flag == 0) {
                System.out.print("Enter command (register/login): ");
                command = getNonEmptyInput(scanner, "Command cannot be empty. Enter 'register' or 'login': ");
                flag = switch (command) {
                    case "register" -> {
                        System.out.print("Username: ");
                        String regUsername = getNonEmptyInput(scanner,
                                "Username cannot be empty.\nPlease enter a username: ");
                        System.out.print("Password: ");
                        String regPassword = getNonEmptyInput(scanner,
                                "Password cannot be empty.\nPlease enter a password: ");

                        AuthRequest registerRequest = new AuthRequest(AuthRequest.REGISTER, regUsername, regPassword);
                        byte[] regRequestBytes = registerRequest.getRequestBytes();
                        out.writeInt(regRequestBytes.length);
                        out.write(regRequestBytes);

                        String message = in.readUTF();
                        System.out.println(message);

                        out.flush();
                        yield 1;
                    }
                    case "login" -> {
                        System.out.print("Username: ");
                        String loginUsername = getNonEmptyInput(scanner,
                                "Username cannot be empty. Please enter a username: ");
                        System.out.print("Password: ");
                        String loginPassword = getNonEmptyInput(scanner,
                                "Password cannot be empty. Please enter a password: ");

                        AuthRequest loginRequest = new AuthRequest(AuthRequest.LOGIN, loginUsername, loginPassword);
                        byte[] loginRequestBytes = loginRequest.getRequestBytes();
                        out.writeInt(loginRequestBytes.length);
                        out.write(loginRequestBytes);

                        String message = in.readUTF();
                        System.out.println(message);

                        out.flush();
                        yield 1;
                    }
                    default -> {
                        System.out.println("Unknown command. Please enter 'register' or 'login'.");
                        yield 0;
                    }
                };
            }

            // Interaction loop
            while (true) {
                System.out.print("Enter command (put/get/multiPut/multiGet/exit): ");
                command = scanner.nextLine();

                switch (command) {
                    case "put" -> {
                        System.out.print("Key: ");
                        String putKey = getNonEmptyInput(scanner, "Key cannot be empty. Please enter a key: ");
                        System.out.print("Value: ");
                        String value = getNonEmptyInput(scanner, "Value cannot be empty. Please enter a value: ");
                        byte[] valueBytes = value.getBytes();
                        client.put(putKey, valueBytes);
                    }
                    case "multiPut" -> {
                        System.out.print("How many values you want to insert: ");
                        int N = scanner.nextInt();
                        HashMap<String, byte[]> pairs = new HashMap<>();

                        for (int i = 0; i < N; i++) {
                            System.out.print("Key: ");
                            String putKey = getNonEmptyInput(scanner, "Key cannot be empty. Please enter a key: ");
                            System.out.print("Value: ");
                            String value = getNonEmptyInput(scanner, "Value cannot be empty. Please enter a value: ");
                            byte[] valueBytes = value.getBytes();
                            pairs.put(putKey, valueBytes);
                        }
                        client.multiPut(pairs);
                    }
                    case "get" -> {
                        System.out.print("Key: ");
                        String getKey = getNonEmptyInput(scanner, "Key cannot be empty. Please enter a key: ");

                        byte[] info = client.get(getKey);
                        if (info != null) {
                            System.out.println("Response: " + new String(info));
                        }
                    }
                    case "multiGet" -> {
                        System.out.print("How many keys you want to retrieve: ");
                        int N = scanner.nextInt();
                        ArrayList<String> keys = new ArrayList<>();

                        for (int i = 0; i < N; i++) {
                            System.out.print("Key: ");
                            String putKey = getNonEmptyInput(scanner, "Key cannot be empty. Please enter a key: ");
                            keys.add(putKey);
                        }

                        Map<String, byte[]> responses = client.multiGet(keys);
                        if (responses != null) {
                            for (Map.Entry<String, byte[]> entry : responses.entrySet()) {
                                System.out.println(
                                        "Key: " + entry.getKey() + " | Value: " + new String(entry.getValue()) + "\n");
                            }
                        }
                    }
                    case "exit" -> {
                        System.out.println("Exiting...");
                        out.writeUTF("exit"); // Envie o comando exit
                        out.flush();
                        client.closeConnection();
                        return;
                    }
                    default -> System.out
                            .println("Unknown command. Please enter 'put', 'get', 'multiPut', 'multiGet' or 'exit'.");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Utility method to get non-empty input from the user
    private static String getNonEmptyInput(Scanner scanner, String errorMessage) {
        String input;
        do {
            input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                System.out.print(errorMessage);
            }
        } while (input.trim().isEmpty());
        return input;
    }
}
