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

    /* Faz o registo do cliente no tracker */
    public void register() throws NodeException, ClientException {
        this.client.sendRegister();
    }

    /* Envia a informação dos ficheiros do cliente para o tracker */
    public void sendFiles() throws ClientException, DirectoryException, FileException {
        this.client.sendFiles();
    }

    /* Pede informações relativas a um ficheiro para download do tracker */
    public void requestFile(String filename) throws CommunicableException, ClientException {
        this.client.requestFile(filename);
    }

    /* Informa o tracker que se vai desligar */
    public void disconnect() throws ClientException {
        this.client.sendDisconnect();
    }
}
