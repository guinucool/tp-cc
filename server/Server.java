package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    
    private ServerSocket sv;
    private int port;
    private Tracker tracker;
    private List<Thread> threads;

    private class ServerException extends Exception {
    
        public ServerException(String msg) {
            super(msg);
        }
    }

    private static class SocketRunner implements Runnable {

        private Tracker tracker;
        private Socket socket;

        public SocketRunner(Tracker tracker, Socket socket) {
            this.tracker = tracker;
            this.socket = socket;
        }

        public void run() {

        }
    }

    public Server() {
        this.port = 9090;
        this.threads = new ArrayList<Thread>();
    }

    public Server(int port) throws ServerException {
        setPort(port);
        this.threads = new ArrayList<Thread>();
    }

    public void setPort(int port) throws ServerException {
        if (port < 0 || port > 65534)
            throw new ServerException("Server port is invalid (" + port + ")");
        
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void start() throws IOException {
        this.sv = new ServerSocket(this.port);
    }

    public void listen() throws IOException {
        while (true) {
            Socket socket = this.sv.accept();

            Thread stt = new Thread(new SocketRunner(this.tracker, socket));
            stt.start();

            this.threads.add(stt);
        }
    }

    public void stop() throws IOException {
        this.sv.close();
    }
}
