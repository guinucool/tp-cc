import java.util.Scanner;

import server.Server;
import server.ServerException;
import thread.ServerWorker;

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

        } catch (ServerException e) {
            System.out.println("Couldn't initialize server! Check if it is connected to a network or if the port is not in use!");
            System.exit(1);   
        }
    }
}
