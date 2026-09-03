package worker;

import message.Requestable;
import runner.Runner;
import runner.RunnerException;

/**
 * Objeto que define o worker que trata de executar um pedido.
 */
public class RequestWorker implements Runnable {
    
    private Runner runner;              /* Runner que irá tratar e enviar o pedido */
    private Requestable resquest;       /* Pedido a ser tratado e enviado */

    /* Construtor parametrizado */
    public RequestWorker(Runner runner, Requestable request) throws WorkerException {

        /* Verifica se o runner ainda está aberto */
        if (runner.isClosed())
            throw new WorkerException("runner-disconnected");

        /* Associa as propriedades */
        this.runner = runner;
        this.resquest = request;
    }

    /* Executa o pedido pretendido */
    public void run() {

        /* Tenta enviar o pedido para o servidor */
        try {
            this.runner.send(this.resquest);
        } catch (RunnerException e) {
            this.resquest.fail(e);
        }
    }
}
