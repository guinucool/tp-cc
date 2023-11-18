package thread;

import server.Server;
import server.ServerException;
import socket.RunnerException;

public class ServerWorker implements Runnable {
    
    private Server server;

    /* Constructors */
    public ServerWorker(Server server) {
        this.server = server;
    }

    /* Thread */
    public void run() {

        while (!this.server.isClosed()) {
            try {
                this.server.listen();
            } catch (ServerException e) {
                System.out.println("Incoming connection refused! Server ran out of resources.");
            } catch (RunnerException e) {
                System.out.println("Incoming request failed to connect!");
            }
        }
    }
}
