package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import exception.*;
import handler.*;
import socket.*;

public class Server implements Runnable {
    
    private ServerSocket server;
    private int port;
    private List<Thread> threads;
    private List<ServerSocketRunner> sockets;
    private ExceptionHandler handler;
    private ReentrantLock lock;

    private Tracker tracker;

    public Server() {
        this.setHandler();
        this.port = 9090;
        this.threads = new ArrayList<>();
        this.sockets = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.tracker = new Tracker();
    }

    public Server(int port) throws InvalidServerPort {
        this.setPort(port);
        this.setHandler();
        this.threads = new ArrayList<>();
        this.sockets = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.tracker = new Tracker();
    }

    private void setHandler() {
        this.handler = new NoHandler();
    }

    private void setPort(int port) throws InvalidServerPort {
        if (port < 0 || port > 65535)
            throw new InvalidServerPort("" + port);

        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public String getIp() throws SocketException {
        String ip = "";
        
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while( networkInterfaceEnumeration.hasMoreElements()) {
            for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                if ( interfaceAddress.getAddress().isSiteLocalAddress())
                    ip = interfaceAddress.getAddress().getHostAddress();
        }

        return ip;
    }

    public void start() throws IOException {
        this.server = new ServerSocket(this.port);
    }

    public void run() {
        while (!(this.server.isClosed())) {
            try {
                Socket socket = this.server.accept();
                this.lock.lock();

                ServerSocketRunner runner = new ServerSocketRunner(socket, this.tracker);
                Thread thread = new Thread(runner);
                thread.start();

                this.threads.add(thread);
                this.sockets.add(runner);

                this.lock.unlock();
            } catch (IOException e) {
                this.handler.handle(e.getMessage());
            }
        }
    }

    public void stop() {
        this.lock.lock();

        try {
            this.server.close();
        } catch (IOException e) {
            this.handler.handle(e.getMessage());
        }

        for (ServerSocketRunner socketRunner : this.sockets)
            socketRunner.shutdown();

        for (Thread thread : this.threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                this.handler.handle(e.getMessage());
            }
        }

        this.lock.unlock();
    }
}
