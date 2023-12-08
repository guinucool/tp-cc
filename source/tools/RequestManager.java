package tools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import message.CommunicableException;
import message.Requestable;
import runner.Runner;
import runner.RunnerException;
import worker.RequestWorker;
import worker.WorkerException;

/**
 * Objeto que define um gestor de pedidos de mensagens e assume que a concorrência
 * será tratada por classes que o utilizarem.
 */
public class RequestManager {
    
    private Map<Short,Requestable> requests;        /* Mapa de requests para identificador de requests */
    private Condition requestWait;                  /* Condição de espera para os pedidos */

    /* Construtor parametrizado */
    public RequestManager(Condition waiter) {
        this.requests = new HashMap<>();
        this.requestWait = waiter;    
    }

    /* Verifica se existe um pedido com o id pretendido */
    public boolean hasRequest(short identifier) {
        return this.requests.containsKey(identifier);
    }

    /* Adiciona uma mensagem de pedido ao cliente */
    private void addRequest(Requestable request) throws RequestException {

        /* Verifica se o pedido não existe no cliente */
        if (this.requests.containsKey(request.getIdentifier()))
            throw new RequestException("request-exists");

        this.requests.put(request.getIdentifier(), request);
    }

    /* Resolver com sucesso um pedido */
    public void solveRequest(short identifier) throws RequestException {
    
        /* Verifica se o pedido não existe no cliente */
        if (!this.requests.containsKey(identifier))
            throw new RequestException("request-undefined");

        this.requests.remove(identifier).solve();

        this.requestWait.signalAll();
    }

    /* Resolve em falhanço uma pedido */
    public void failRequest(short identifier, RunnerException e) throws RequestException {

        /* Verifica se o pedido existe no cliente */
        if (!this.requests.containsKey(identifier))
            throw new RequestException("request-undefined");

        this.requests.remove(identifier).fail(e);

        this.requestWait.signalAll();
    }

    /* Resolve em falhanço todas as mensagens de pedido */
    public void failAllRequest(RunnerException e) {

        /* Falha todos os pedidos existentes */
        for (Short identifier : this.requests.keySet())
            this.requests.remove(identifier).fail(e);

        this.requestWait.signalAll();
    }

    /* Procura e remove por pedidos que já estajam resolvidos */
    public void updateAllRequest(RunnerException e) {

        /* Verifica todos os pedidos existentes */
        for (Short identifier : this.requests.keySet())
            if (this.requests.get(identifier).isResolved())
                this.requests.remove(identifier);
    }

    /* Envia um pedido através de um runner */
    public void sendSequentialRequest(Runner runner, Requestable request) throws CommunicableException, RequestException {

        /* Verifica se o pedido já não existe e adiciona-o ao manager */
        this.addRequest(request);

        try {

            /* Cria uma nova thread que irá tratar do pedido */
            Thread thread = new Thread(new RequestWorker(runner, request));
            thread.start();

            /* Espera por uma resolução */
            while (!request.isResolved()) {
                try {
                    this.requestWait.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    /* Não é preciso tratar a exceção */
                }
            }

            /* Verifica a razão do falhanço */
            if (request.isFailed())
                request.exceptionThrow();

        } catch (CommunicableException e) {
            throw e;
        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }
    }

    /* Envia um pedido através de um runner */
    public void sendConcurrentRequest(Runner runner, Requestable request) throws RequestException {

        /* Verifica se o pedido já não existe e adiciona-o ao manager */
        this.addRequest(request);

        /* Cria uma nova thread que irá tratar do pedido */
        try {
            Thread thread = new Thread(new RequestWorker(runner, request));
            thread.start();
        } catch (WorkerException e) {
            throw new RequestException(e.getMessage());
        }
    }

    /* Converte o manager para formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(RequestManager)");
        builder.append("requests:");

        for (Map.Entry<Short,Requestable> request : this.requests.entrySet()) {
            builder.append(" key:").append(request.getKey());
            builder.append(" value:").append(request.getValue());
        }

        builder.append(";");

        return builder.toString();
    }
}
