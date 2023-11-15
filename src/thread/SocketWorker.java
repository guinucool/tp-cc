package thread;

import java.io.*;
import java.net.*;

import sockexcp.*;

public class SocketWorker {
    
    private SocketRunner runner;
    private Socket socket;

    /* Constructors */
    public SocketWorker(Socket socket) {
        this.socket = socket;
    }

    /* Thread */
    public void run() {
        
    }
}
