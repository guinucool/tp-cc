package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

import socket.*;
import thread.*;
import tracker.*;

/**
 * Objeto que define o servidor.
 * 
 * @param listener
 *      Objeto que espera e "ouve" pela conexão de novos sockets.
 * 
 * @param runners
 *      Os vários runners que correm os sockets ligados ao servidor.
 * 
 * @param threads
 *      As threads que sustentam os vários runners.
 */
public class Server {
    
    private Tracker tracker;
    private ServerSocket listener;
    private List<SocketRunner> runners;
    private List<Thread> threads;

    /* Locks */
    private Lock lock = new ReentrantLock();

    /* Constructors */
    public Server() throws IOException {
        this.tracker = new Tracker();
        this.listener = new ServerSocket(9090);
        this.runners = new ArrayList<>();
        this.threads = new ArrayList<>();
    }

    public Server(int port) throws IOException {
        this.tracker = new Tracker();
        this.listener = new ServerSocket(port);
        this.runners = new ArrayList<>();
        this.threads = new ArrayList<>();
    }

    /* Getters */
    public String getIp() throws SocketException {
        String ip = "localhost";
        
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();

        while( networkInterfaceEnumeration.hasMoreElements()) {
            for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                if ( interfaceAddress.getAddress().isSiteLocalAddress())
                    ip = interfaceAddress.getAddress().getHostAddress();
        }

        return ip;
    }

    public int getPort() {
        return this.listener.getLocalPort();
    }

    /* Checkers */
    public boolean isClosed() {

        this.lock.lock();
        
        try {
            return this.listener.isClosed();
        } finally {
            this.lock.unlock();
        }
    }

    /* Auxiliar */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Server)listener:").append(this.listener).append(";");
        builder.append("runners:").append(this.runners).append(";");
        builder.append("threads:").append(this.threads).append(";");
        
        return builder.toString();
    }

    public void listen() throws IOException, SocketRunnerException {

        Socket client = this.listener.accept();

        this.lock.lock();
        try {
            SocketRunner runner = new ServerSocketRunner(client);
            Thread worker = new Thread(new SocketWorker(runner, this.tracker));
            worker.start();

            this.runners.add(runner);
            this.threads.add(worker);
        } finally {
            this.lock.unlock();
        }
    }

    public void close() {

        this.lock.lock();

        try {
            this.listener.close();
        } catch (IOException e) {
            /* Não é necessário tratar esta exceção */
        }

        for (SocketRunner runner : this.runners) {
            if (!runner.isClosed())
                runner.close();
        }

        for (Thread worker : this.threads) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                /* Não é necessário tratar nada */
            }
        }

        this.lock.unlock();
    }
}
