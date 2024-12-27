package client;

import common.AuthRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The <code>ClientInterface</code> class provides a command-line interface for
 * interacting with a client. It allows users to authenticate (either by
 * registering or logging in), and perform actions like storing and retrieving
 * data from the server, using commands such as <code>put</code>,
 * <code>get</code>, <code>multiPut</code>, and <code>multiGet</code>. The class
 * communicates with the server via a socket connection, sending and receiving
 * data in a serialized format.
 * <p>
 * It uses the <code>Client</code> class to manage the socket connection and
 * handle the data transmission. The user can interact with the client through
 * text-based commands entered into the console.
 */
public class ClientInterface {

    /**
     * The entry point of the application. This method establishes the
     * connection to the server, handles user authentication, and provides a
     * loop for interacting with the server.
     * <p>
     * The user can enter commands such as <code>register</code>,
     * <code>login</code>, <code>put</code>, <code>get</code>,
     * <code>multiPut</code>, <code>multiGet</code>, or <code>exit</code>.
     * Depending on the command, appropriate actions are taken, such as sending
     * requests to the server and displaying responses.
     *
     * @param args command-line arguments (NA)
     */
    public static void main(String[] args) {
        Client client = new Client();

        try (
                DataInputStream in = client.getInputStream(); DataOutputStream out = client.getOutputStream(); Scanner scanner = new Scanner(System.in)) {

            int flag = 0;
            String command;
            Console console = System.console(); // Obtém o console do sistema

            if (console == null) {
                System.err.println("Console not available. Password input will not be masked.");
            }

            // Authentication loop
            while (flag == 0) {
                System.out.print("Enter command (register/login): ");
                command = getNonEmptyInput(scanner, "Command cannot be empty. Enter 'register' or 'login': ");
                flag = switch (command) {
                    case "register" -> {
                        System.out.print("Username: ");
                        String regUsername = getNonEmptyInput(scanner,
                                "Username cannot be empty.\nPlease enter a username: ");
                        String regPassword = readPassword(console, scanner, "Password cannot be empty.\nPlease enter a password: ");

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
                        String loginPassword = readPassword(console, scanner, "Password cannot be empty. Please enter a password: ");

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
                System.out.print("Enter command (put/get/multiPut/multiGet/getWhen/exit): ");
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
                    case "getWhen" -> {
                        System.out.print("Key: ");
                        String getKey = getNonEmptyInput(scanner, "Key cannot be empty. Please enter a key: ");
                        System.out.print("Key Condition: ");
                        String getKeyCond = getNonEmptyInput(scanner, "Key Condition cannot be empty. Please enter a key: ");
                        System.out.print("Value Condition: ");
                        String getValueCond = getNonEmptyInput(scanner, "Value Condition cannot be empty. Please enter a key: ");
                        byte[] valueCondBytes = getValueCond.getBytes();

                        byte[] info = client.getWhen(getKey, getKeyCond, valueCondBytes);
                        if (info != null) {
                            System.out.println("Response: " + new String(info));
                        }
                    }
                    case "exit" -> {
                        System.out.println("Exiting...");
                        out.writeUTF("exit"); // Envie o comando exit
                        out.flush();
                        client.closeConnection();
                        return;
                    }
                    default ->
                        System.out
                                .println("Unknown command. Please enter 'put', 'get', 'multiPut', 'multiGet' or 'exit'.");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Prompts the user for non-empty input. If the user enters an empty string,
     * the input is requested again.
     *
     * @param scanner the scanner used to read the input
     * @param errorMessage the message to be displayed if the input is empty
     * @return the non-empty user input
     */
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

    /**
     * Reads the password from the user input. If a console is available, the
     * password is masked. Otherwise, the password is entered as plain text.
     *
     * @param console the system console (may be <code>null</code>)
     * @param scanner the scanner used if the console is unavailable
     * @param errorMessage the message to be displayed if the password is empty
     * @return the user-entered password
     */
    private static String readPassword(Console console, Scanner scanner, String errorMessage) {
        if (console != null) {
            char[] passwordArray = console.readPassword("Password: ");
            return new String(passwordArray);
        } else {
            // Fallback if console is not available
            System.out.print("Password: ");
            return getNonEmptyInput(scanner, errorMessage);
        }
    }
}
