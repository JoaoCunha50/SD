package client;

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
            
            while (flag == 0) {
                // Authentication
                System.out.print("Enter command (register/login): ");
                command = scanner.nextLine();
                switch (command) {
                    case "register":
                        break;
                    case "login":
                        flag = 1;
                        break;
                    default:
                        System.out.println("Unknown command");
                }
            }

            // Interaction loop
            while (true) {
                System.out.print("Enter command (put/get/exit): ");
                command = scanner.nextLine();
                String key;
                switch (command) {
                    case "put":
                        System.out.print("Key: ");
                        key = scanner.nextLine();
                    case "get":
                        System.out.print("Key: ");
                        key = scanner.nextLine();
                    case "exit":
                        break;
                    default:
                        System.out.println("Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
