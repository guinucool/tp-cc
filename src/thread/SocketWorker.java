package thread;

import java.io.*;
import java.net.*;

import socket.*;

public class SocketWorker implements Runnable {
    
    private SocketRunner runner;

    /* Constructors */
    public SocketWorker(SocketRunner runner) {
        this.runner = runner;
    }

    /* Thread */
    public void run() {
        
    }
}
