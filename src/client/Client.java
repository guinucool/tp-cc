package client;

import java.io.*;
import java.net.*;
import java.util.*;

import controller.*;
import files.*;
import model.file.NodeFile;
import socket.*;
import view.*;

public class Client {
    
    private SocketControl control;
    private SocketView view;
    private Directory directory;

    /* Constructors */
    public Client(String path, String ip, int port) throws PathException, UnknownHostException, IOException, SocketRunnerException {
        this.directory = new Directory(path);

        Socket socket = new Socket(ip, port);
        SocketRunner runner = new ClientSocketRunner(socket);

        this.control = new SocketControl(runner);
        this.view = new SocketView(runner);
    }

    /* Getters */
    public String getPath() {
        return this.directory.getPath();
    }

    public List<NodeFile> getFiles() {
        return this.directory.getFiles(false);
    }

    public String getSourceIP() {
        return this.view.getSourceIP();
    }

    public String getDestIP() {
        return this.view.getDestIP();
    }

    public int getSourcePort() {
        return this.view.getSourcePort();
    }

    public int getDestPort() {
        return this.view.getDestPort();
    }

    /* Checkers */
    public boolean isClosed() {
        return this.view.isClosed();
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

    public void register() throws ClientException {
        try {
            this.control.sendRegister(9090);
        } catch (IOException | SocketControlException e) {
            this.control.close();
            throw new ClientException.ClientRejectedConnectionException(e.getMessage());
        }
    }

    public void updateServerFiles() throws ClientException {
        List<NodeFile> files = this.directory.getFiles(true);

        try {
            for (NodeFile file : files) {
                this.control.sendFile(file);
                this.directory.sendFile(file.getHash());
            }
        } catch (SocketControlException.RepeatedFilenameException e) {
            throw new ClientException.FilenameExistsServerException(e.getMessage());
        } catch (SocketControlException.FileRefusedException e) {
            throw new ClientException.ServerRejectedFileException(e.getMessage());
        } catch (IOException | SocketControlException e) {
            this.control.close();
            throw new ClientException.ClientRejectedConnectionException(e.getMessage());
        } catch (PathException.FileNotInPathException e) {
            /* Não é preciso tratar esta exceção */
        }
    }

    public void disconnect() throws ClientException {
        try {
            this.control.sendDisconnect();
        } catch (IOException | SocketControlException e) {
            throw new ClientException.ClientRejectedConnectionException(e.getMessage());
        } finally {
            this.control.close();
        }
    }
}
