package controller;

import file.DirectoryException;
import message.CommunicableException;
import model.Client;
import tools.RequestException;

public class RunnerClientController {
    
    private static RunnerClientController singleton;

    private Client client;

    private RunnerClientController() {
        this.client = Client.getInstance();
    }

    public static RunnerClientController getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new RunnerClientController();

        /* Devolve a instância global */
        return singleton;
    }

    /* Resolve um pedido em sucesso */
    public void solveRequest(short identifier) throws RequestException {
        this.client.solveRequest(identifier);
    }

    /* Falha um pedido */
    public void failRequest(short identifier, CommunicableException e) throws RequestException {
        this.client.failRequest(identifier, e);
    }

    /* Desliga o cliente */
    public void close() {
        this.client.stop();
    }

    /* Confirma um pedido enviado para o servidor */
    public void notifyRepeteadFilename(String filename) throws DirectoryException {
        this.client.updateFileStatus(filename);
    }

    public void receiveFile(byte[] data) {

    }

    public void receiveBlock(byte[] data) {
        
    }
}
