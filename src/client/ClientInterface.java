package client;

import common.AuthRequest;
import common.TasksRequest;
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
                flag = switch (command) {
                    case "register" -> {
                        System.out.print("Username: ");
                        String regUsername = scanner.nextLine();
                        System.out.print("Password: ");
                        String regPassword = scanner.nextLine();
                        
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
                        String loginUsername = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginPassword = scanner.nextLine();

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
                System.out.print("Enter command (put/get/exit): ");
                command = scanner.nextLine();
                byte[] taskBytes;
                switch (command) {
                    case "put" -> {
                        System.out.print("Key: ");
                        String putKey = scanner.nextLine();
                        System.out.print("Value: ");
                        String value = scanner.nextLine();
                        TasksRequest task = new TasksRequest(TasksRequest.PUT, putKey, value);
                        taskBytes = task.getTaskBytes();
                        out.writeInt(taskBytes.length);
                        out.write(taskBytes);
                        System.out.println("Task sent.");
                        String response = in.readUTF();
                        System.out.println("Response: " + response);
                    }
                    case "get" -> {
                        System.out.print("Key: ");
                        String getKey = scanner.nextLine();
                        TasksRequest task = new TasksRequest(TasksRequest.GET, getKey, null);
                        taskBytes = task.getTaskBytes();
                        out.writeInt(taskBytes.length);
                        out.write(taskBytes);
                        System.out.println("Task sent.");
                        int length = in.readInt();
                        byte[] info = new byte[length];
                        in.read(info);
                        System.out.println("Response: " + new String(info));
                    }
                    case "exit" -> {
                        TasksRequest task = new TasksRequest(TasksRequest.EXIT, null, null);
                        taskBytes = task.getTaskBytes();
                        System.out.println("Exiting...");
                        out.writeInt(taskBytes.length);
                        out.write(taskBytes); // Envie o comando exit
                        out.flush();
                        client.closeConnection(in, out);
                        return;
                    }
                    default -> System.out.println("Unknown command. Please enter 'put', 'get', or 'exit'.");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
