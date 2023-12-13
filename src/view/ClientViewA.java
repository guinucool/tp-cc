package view;

import client.Client;
import client.ClientException;
import model.file.NodeFile;

public class ClientViewA {
    
    private Client client;

    /* Constructors */
    public ClientView(Client client) {
        this.client = client;
    }

    /* View */
    public boolean isClosed() {
        return this.client.isClosed();
    }

    public void printPath() {
        System.out.println("Client is running on path " + this.client.getPath());
    }

    public void printFiles() {

        System.out.println("Files on directory:");

        for (NodeFile file : this.client.getFiles())
            System.out.println("File - Hash: " + file.getHash() + " Filename: " + file.getName() + " Size: " + file.getSize() + " Sent: " + file.isSent());
    }

    public void printIp() throws ClientException {
        System.out.println("Client is running on " + this.client.getIp() + " port 9090");
    }
}
