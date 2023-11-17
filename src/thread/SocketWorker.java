package thread;

import java.io.*;
import java.net.*;

import sockexcp.*;

public class SocketWorker implements Runnable {
    
    private SocketRunner runner;

    /* Constructors */
    public SocketWorker(SocketRunner runner) {
        this.socket = runner;
    }

    /* Thread */
    public void run() {
        
    }
}
