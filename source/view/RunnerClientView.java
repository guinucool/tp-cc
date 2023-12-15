package view;

import model.Client;

public class RunnerClientView {
    
    private static RunnerClientView singleton;

    private Client client;

    private RunnerClientView() {
        this.client = Client.getInstance();
    }

    public static RunnerClientView getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new RunnerClientView();

        /* Devolve a instância global */
        return singleton;
    }

    /* Verifica se existe um pedido para o identificador fornecido */
    public boolean hasRequest(short identifier) {
        return this.client.wasRequested(identifier);
    }
}
