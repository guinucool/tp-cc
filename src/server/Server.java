package server;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import socket.SocketRunner;
import socket.ServerSocketRunner;
import socket.RunnerException;
import thread.SocketWorker;

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
    
    private ServerSocket listener;
    private List<SocketRunner> runners;
    private List<Thread> threads;

    /* Locks */
    private Lock lock = new ReentrantLock();

    /* Constructors */
    public Server() throws ServerException {

        try {
            this.listener = new ServerSocket(9090);
            this.runners = new ArrayList<>();
            this.threads = new ArrayList<>();
        } catch (IOException e) {
            throw new ServerException("server failed to start");
        }
    }

    public Server(int port) throws ServerException {

        try {
            this.listener = new ServerSocket(port);
            this.runners = new ArrayList<>();
            this.threads = new ArrayList<>();
        } catch (IOException e) {
            throw new ServerException("server failed to start");
        }
    }

    /* Getters */
    public String getIp() throws ServerException {

        try {
            String ip = "";
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();

            while( networkInterfaceEnumeration.hasMoreElements()) {
                for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                    if ( interfaceAddress.getAddress().isSiteLocalAddress())
                        ip = interfaceAddress.getAddress().getHostAddress();
            }

            return ip;
        
        } catch (SocketException e) {
            throw new ServerException("server not connected to a network");
        }
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

    public void listen() throws ServerException, RunnerException {

        try {
            Socket client = this.listener.accept();

            this.lock.lock();
            try {
                SocketRunner runner = new ServerSocketRunner(client);
                Thread worker = new Thread(new SocketWorker(runner));
                worker.start();

                this.runners.add(runner);
                this.threads.add(worker);
            } finally {
                this.lock.unlock();
            }

        } catch (IOException e) {
            if (!this.isClosed())
                throw new ServerException("server ran out of resources");
        }
    }

    public void close() {

        this.lock.lock();

        try {
            if (!this.listener.isClosed()) this.listener.close();
        } catch (IOException e) {
            /* Não é necessário tratar esta exceção */
        }

        for (SocketRunner runner : this.runners)
            runner.close();

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
