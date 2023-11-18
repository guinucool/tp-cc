import java.io.*;
import java.net.*;
import java.util.*;

import server.*;
import thread.*;

public class FS_Tracker {
    
    public static void main(String[] args) {

        try {
            Server server = new Server();
            System.out.println("Server active at " + server.getIp() + " port " + server.getPort());

            Thread worker = new Thread(new ServerWorker(server));
            worker.start();

            Scanner scanner = new Scanner(System.in);
            while (!scanner.nextLine().equals("STOP"));
            scanner.close();

            server.close();
            System.exit(0);

        } catch (SocketException e) {
            System.out.println("Server lost connection.");
            System.exit(1);
            
        } catch (IOException e) {
            System.out.println("Failed to open server on the given port. Maybe it is already in use?");
            System.exit(1);
        }
    }
}
