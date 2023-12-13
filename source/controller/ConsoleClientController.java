package controller;

import model.ClientException;
import file.DirectoryException;
import file.FileException;
import message.CommunicableException;
import model.Client;
import model.NodeException;

public class ConsoleClientController {
    
    private static ConsoleClientController singleton;

    private Client client;

    private ConsoleClientController() {
        this.client = Client.getInstance();
    }

    public static ConsoleClientController getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new ConsoleClientController();

        /* Devolve a instância global */
        return singleton;
    }

    public void register() throws CommunicableException, NodeException, ClientException {
        this.client.sendRegister();
    }

    public void sendFiles() throws ClientException, DirectoryException, FileException, CommunicableException {
        this.client.sendFiles();
    }

    public void requestFile() {

    }

    public void disconnect() throws CommunicableException, ClientException {
        this.client.sendDisconnect();
    }
}
