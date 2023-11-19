package controller;

import client.Client;
import client.ClientException;
import socket.RunnerException;

public class ClientControl {
    
    private Client client;

    /* Constructors */
    public ClientControl(Client client) {
        this.client = client;
    }

    /* Control */
    public void updateFiles() {
        this.client.updateDirectory();
    }

    public void updateServer() throws ClientException, RunnerException {
        this.client.updateDirectory();
        this.client.sendUpdate();
    }

    public void sendDisconnect() throws ClientException, RunnerException {
        this.client.sendDisconnect();
    }
}
