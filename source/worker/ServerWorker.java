package worker;

import server.ServerException;
import server.Server;

/**
 * Objeto que trata de manter o funcionamento constante da audição de pedidos de um
 * servidor.
 */
public class ServerWorker implements Runnable {
    
    private Server server;          /* Servidor que irá ser corrido por este worker */

    /* Construtor parametrizado */
    public ServerWorker(Server server) {
        this.server = server;
    }

    /* Funcionamento do worker */
    public void run() {

        /* Mantém a audição enquanto o servidor estiver aberto */
        while (!this.server.isClosed()) {

            /* Ouve por pedidos que possam chegar (mensagens ou conexões) */
            try {
                this.server.listen();
            } catch (ServerException e) {

                /* Notifica o servidor em caso de esgotamento de recursos */
                if(e.getMessage().equals("server-resources"))
                    System.out.println("Server ran out of resources! Could not accept incoming connection...");

                /* Notifica o servidor em caso de perda de conexão */
                if(e.getMessage().equals("runner-disconnected"))
                    System.out.println("An incoming connection was lost...");
            }
        }
    }
}
