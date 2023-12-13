package controller;

import model.Client;

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

    public void solveRequest() {

    }

    public void failRequest() {

    }

    public void failAllRequest() {

    }

    public void confirmFile() {

    }

    public void receiveFile() {

    }

    public void receiveBlock() {
        
    }
}
