package thread;

import java.io.*;

import server.*;
import socket.*;

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
            } catch (SocketRunnerException | IOException e) {
                /* Não é necessário tratar as exceções */
            }
        }
    }
}
