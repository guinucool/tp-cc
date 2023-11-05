import java.io.*;

import client.*;
import client.Console;

public class FSNode {
    
    public static void main(String[] args) {

        if (args.length > 3 || args.length < 2) {
            System.out.println("Invalid arguments.");
            return;
        }

        Client client = new Client(args[1]);

        if (args.length == 3) {
            try {
                client.setPort(Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                System.out.println("The port given is not numeric.");
                return;
            }
        }

        try {
            client.start();
        } catch (IOException e) {
            client.shutdown();
            System.out.println("Server is unreachable. Please, try again later...");
            return;
        }

        Console console = new Console(client);
        console.start();
    }
}