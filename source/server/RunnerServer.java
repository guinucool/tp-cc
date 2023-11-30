package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import message.Message;
import runner.Runner;
import runner.RunnerException;
import worker.MessageWorker;
import worker.WorkerException;

/**
 * Objeto que define um servidor que usa como base da sua audição um runner que recebe mensagens.
 * Este assume que o runner fornecido já possuí o devido tratamento de concorrência.
 */
public class RunnerServer implements Server {
    
    private Runner runner;              /* Runner usado como servidor de audição */
    private MessageWorker template;     /* Template de operador de mensagem para operar futuras mensagens */

    private List<Thread> workers;       /* Lista de threads que correm os vários operadores de mensagem */

    private Lock write = new ReentrantLock();                           /* Lock de escrita do servidor */
    private Lock reinforcedReceive = new ReentrantLock();               /* Lock de reforço à receção de mensagens */

    /* Construtor argumentado */
    public RunnerServer(Runner runner, MessageWorker template) throws ServerException {
        if (runner.isClosed())
            throw new ServerException("runner-disconnected");

        this.runner = runner;
        this.template = template;
        this.workers = new ArrayList<>();
    }

    /* Endereço local onde se encontra alojado o servidor */
    public String getAddress() {
        return this.runner.getAddress();
    }

    /* Porta local onde se encontra alojado o servidor */
    public int getPort() {
        return this.runner.getPort();
    }

    /* Espera pela receção de novas mensagens ao servidor */
    public void listen() {

        /* Ouve pela chegada de uma mensagem vinda do runner e opera sobre a mesma */
        try {

            /* Placeholder para a futura mensagem que irá ser recebida */
            Message income;

            /* Bloqueio para garantir a ordem de chegada das mensagens */
            this.reinforcedReceive.lock();

            /* Receção das mensagens */
            try {
                income = this.runner.receive();
                this.write.lock();
            } finally {
                this.reinforcedReceive.unlock();
            }

            /* Operação da mensagem */
            try {
                this.operate(income);
            } finally {
                this.write.unlock();
            }

        } catch (RunnerException | WorkerException e) {
            this.stop();
            this.template.close(false);
        }
    }

    /**
     * Opera uma mensagem recebida pelo runner do servidor
     * 
     * (É esperado que o tratamento de concorrência desta função seja tratado
     * nas funções que a utilizam)
     */
    private void operate(Message message) throws WorkerException {
        /* Caso o runner tenha sido propositadamente fechado */
        if (message == null) {
            this.stop();
            this.template.close(true);
            return;
        }

        /* Cria um operador de mensagem */
        MessageWorker operator = this.template.operateMessage(message);

        Thread worker = new Thread(operator);
        worker.start();

        /* Adiciona o worker à lista de workers */
        this.workers.add(worker);
    }

    /* Para todos os processos do servidor */
    public void stop() {

        /* Fecha o runnner */
        this.runner.close();

        /* Bloqueia a operação de escrita */
        this.write.lock();

        /* Espera pelo término de todas as threads */
        for (Thread worker : this.workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                /* Não é preciso tratar esta exceção */
            }
        }
        this.write.unlock();
    }

    /* Verifica se o servidor está fechado */
    public boolean isClosed() {
        return this.runner.isClosed();
    }

    /* Conversão do runner server para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Runner)(Server)");
        builder.append("runner:").append(this.runner.toString()).append(";");
        
        return builder.toString();
    }
}
