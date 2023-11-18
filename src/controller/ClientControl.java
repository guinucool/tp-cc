package controller;

import client.*;

public class ClientControl {
    
    private Client client;

    /* Constructors */
    public ClientControl(Client client) {
        this.client = client;
    }

    /* Control */
    public void registerClient() throws ClientException {
        this.client.register();
        this.client.updateServerFiles();
    }

    public void updateFiles() throws ClientException {
        this.client.
    }
}
