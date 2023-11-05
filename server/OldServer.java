package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class OldServer {
    
    private ServerSocket sv;
    private int port;
    private Tracker tracker;
    private List<Thread> threads;

    public static class ServerException extends Exception {
    
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
            try {
                Node n = new Node(this.socket);
                n.listenRequest();
            } catch (IOException e) {
                System.out.println("Socket connection was lost. Closing socket...");
            } finally {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    System.out.println("Socket disconnect incorrectly. Data may have been corrupted.");
                }
            }
        }
    }

    public OldServer() {
        this.port = 9090;
        this.threads = new ArrayList<Thread>();
    }

    public OldServer(int port) throws ServerException {
        setPort(port);
        this.threads = new ArrayList<Thread>();
    }

    private void setPort(int port) throws ServerException {
        if (port < 0 || port > 65535)
            throw new ServerException("Server port " + port + " is invalid.");
        
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void start() throws IOException {
        this.sv = new ServerSocket(this.port);
    }

    public void listen() {
        while (true) {
            try {
                Socket socket = this.sv.accept();

                Thread stt = new Thread(new SocketRunner(this.tracker, socket));
                stt.start();

                this.threads.add(stt);
            } catch (IOException e) {
                return;
            }
        }
    }

    public void stop() throws IOException {
        this.sv.close();
    }
}
