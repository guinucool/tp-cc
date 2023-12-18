import java.net.UnknownHostException;
import java.util.Scanner;

import server.ServerException;
import server.SocketServer;
import worker.ServerWorker;
import tools.DNS;

public class FSTracker {
    
    public static void main(String[] args) {

        try {
            SocketServer server = new SocketServer();
            System.out.println("Server active at " + DNS.getName(server.getAddress()) + " port " + server.getPort() + ".");

            Thread worker = new Thread(new ServerWorker(server));
            worker.start();

            Scanner scanner = new Scanner(System.in);
            while (!scanner.nextLine().equals("STOP"));
            scanner.close();

            server.stop();

        } catch (ServerException | UnknownHostException e) {
            System.out.println("Couldn't initialize server! Check if it is connected to a network or if the port is not in use!");
            System.exit(1);   
        }
    }
}
