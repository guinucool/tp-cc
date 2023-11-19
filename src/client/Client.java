package client;

import java.io.IOException;
import java.net.Socket;

import java.util.List;
import java.util.Arrays;

import message.TrackMessage;
import message.TrackMessage.Type;

import model.directory.Directory;
import model.directory.PathException;
import model.file.NodeFile;

import socket.SocketRunner;
import socket.ClientSocketRunner;
import socket.RunnerException;
import client.ClientException.FilenameExistsException;;

public class Client {
    
    private Directory directory;
    private SocketRunner runner;

    /* Constructors */
    public Client(String path, String ip) throws PathException, ClientException, RunnerException {
        this.directory = new Directory(path);

        try {
            Socket socket = new Socket(ip, 9090);
            this.runner = new ClientSocketRunner(socket);  
        } catch (IOException e) {
            throw new ClientException("socket-failed");
        }
    }

    public Client(String path, String ip, int port) throws PathException, ClientException, RunnerException {
        this.directory = new Directory(path);

        try {
            Socket socket = new Socket(ip, port);
            this.runner = new ClientSocketRunner(socket);  
        } catch (IOException e) {
            throw new ClientException("socket-failed");
        }
    }

    /* Getters */
    public String getPath() {
        return this.directory.getPath();
    }

    public List<NodeFile> getFiles() {
        return this.directory.getFiles(false);
    }

    public String getSourceIP() {
        return this.runner.getSourceIP();
    }

    public String getDestIP() {
        return this.runner.getDestIP();
    }

    public int getSourcePort() {
        return this.runner.getSourcePort();
    }

    public int getDestPort() {
        return this.runner.getDestPort();
    }

    /* Checkers */
    public boolean isClosed() {
        return this.runner.isClosed();
    }

    /* Auxiliar */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Client)directory:").append(this.directory.getPath()).append(";");

        return builder.toString();
    }

    public void updateDirectory() {
        this.directory.readFiles();
    }

    public void sendRegister() throws RunnerException, ClientException {

        this.runner.sendMessage(new TrackMessage(Type.REQUEST, 100, Arrays.asList("9090")));
        TrackMessage res = this.runner.listenMessage();

        if (!res.isSucessResponse()) {
            this.runner.close();
            throw new ClientException("runner-unsafe");
        }
    }

    public void sendUpdate() throws ClientException, RunnerException {

        List<NodeFile> files = this.directory.getFiles(true);

        for (NodeFile file : files) {
            this.runner.sendMessage(file.toMessage());
            TrackMessage res = this.runner.listenMessage();

            if (res.isTarget(Type.RESPONSE, 201, 1))
                throw new FilenameExistsException(res.getArguments().get(0));

            if (!res.isSucessResponse()) {
                this.runner.close();
                throw new ClientException("runner-unsafe");
            }

            try {
                this.directory.sendFile(file.getHash());
            } catch (PathException e) {
                /* Não é preciso tratar esta exceção */
            }
        }
    }

    public void sendDisconnect() throws ClientException, RunnerException {

        this.runner.sendMessage(new TrackMessage(Type.REQUEST, 101, Arrays.asList()));
        TrackMessage res = this.runner.listenMessage();

        this.runner.close();

        if (!res.isSucessResponse())
            throw new ClientException("runner-unsafe");
    }
}
