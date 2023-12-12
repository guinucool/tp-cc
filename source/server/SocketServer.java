package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import runner.frame.FrameRunner;
import tools.Network;
import worker.ServerFrameWorker;
import worker.ServerWorker;
import worker.WorkerException;
import runner.RunnerException;

/**
 * Objeto que define um servidor socket que aceita pedidos de conexão do tipo
 * socket tcp.
 */
public class SocketServer implements Server {
    
    private ServerSocket listener;          /* Ouvido de pedidos de conexão socket */

    private List<RunnerServer> runners;     /* Lista de runners de sockets ligados ao servidor */
    private List<Thread> workers;           /* Lista de workers que operam os runners ligados ao servidor */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();    /* Lock de leitura e escrita */
    private Lock read = this.rwlock.readLock();                     /* Lock de escrita */
    private Lock write = this.rwlock.writeLock();                   /* Lock de escrita e leitura */

    private Lock receive = new ReentrantLock();                     /* Lock de receção de pedidos */

    /* Construtor vazio (inicia o servidor sempre com a porta 9090) */
    public SocketServer() throws ServerException {

        /* Inicia o servidor com a porta 9090 */
        try {
            this.listener = new ServerSocket(9090);
            this.runners = new ArrayList<>();
            this.workers = new ArrayList<>();
        } catch (IOException e) {
            throw new ServerException("server-failed");
        }
    }

    /* Construtor parametrizado (inicia o servidor com a porta fornecida) */
    public SocketServer(int port) throws ServerException {

        /* Inicia o servidor com a porta fornecida */
        try {
            this.listener = new ServerSocket(port);
            this.runners = new ArrayList<>();
            this.workers = new ArrayList<>();
        } catch (IOException e) {
            throw new ServerException("server-failed");
        }
    }

    /* Endereço de alojamento do servidor de sockets */
    public String getAddress() {

        /* Blqueia a operação de escrita */
        this.read.lock();

        /* Descobre o endereço de alojamento do servidor */
        try {
            if (this.isClosed())
                return "0.0.0.0/0.0.0.0";

            return Network.getLocalAddress();
        } finally {
            this.read.unlock();
        }
    }

    /* Porta de alojamento do servidor de sockets */
    public int getPort() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Devolve a porta do listener do servidor */
        try {
            return this.listener.getLocalPort();
        } finally {
            this.read.unlock();
        }
    }

    /* Ouve pela chegada e pedido de conexão de novos sockets */
    public void listen() throws ServerException {
        try {

            /* Variável que irá armazenar o socket recém-chegado */
            Socket client;

            /* Bloqueia a operação de receção */
            this.receive.lock();

            /* Ouve oela conexão de um novo socket */
            try {
                client = this.listener.accept();
                this.write.lock();
            } finally {
                this.receive.unlock();
            }

            /* Opera sobre o socket criando um processo para o mesmo */
            try {
                this.operate(client);
            } finally {
                this.write.unlock();
            }

        } catch (SocketException e) {
            return;
        } catch (IOException e) {
            throw new ServerException("server-resources");
        } catch (RunnerException | WorkerException e) {
            throw new ServerException("runner-disconnected");
        }
    }

    /**
     * Opera sobre o socket client recebido de forma a criar um processo próprio para este.
     * 
     * @throws RunnerException no caso de o socket ter perdido conexão.
     * @throws ServerException no caso de o runner ter perdido conexão.
     */
    private void operate(Socket client) throws RunnerException, ServerException, WorkerException {

        /* Criação do runner e do seu respetivo servidor */
        FrameRunner runner = new FrameRunner(client);
        RunnerServer server = new RunnerServer(runner, new ServerFrameWorker(runner));

        /* Criação e execução da thread */
        Thread worker = new Thread(new ServerWorker(server));
        worker.start();

        /* Adição da thread e do runner como processos do servidor */
        this.runners.add(server);
        this.workers.add(worker);
    }

    /* Para a execução do servidor de sockets e todos os seus sockets */
    public void stop() {

        /* Bloqueia a operação de leitura e escrita */
        this.write.lock();

        /* Fecha o serviço do servidor em si */
        this.close();

        /* Fecha os vários sockets associados ao servidor */
        for (RunnerServer runner : this.runners)
            runner.stop();

        /* Espera pelo término de todos os workers */
        for (Thread worker : this.workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                /* Não é preciso tratar esta exceção */
            }
        }

        /* Desbloqueia a lock */
        this.write.unlock();
    }

    /* Fecha o serviço de audição do servidor em si */
    private void close() {

        /* Fecha o servidor */
        try {
            if (!this.listener.isClosed())
                this.listener.close();
        } catch (IOException e) {
            /* Não é preciso tratar esta exceção */
        }
    }

    /* Verifica se o servidor de sockets está fechado */
    public boolean isClosed() {
        
        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Devolve o estado de fecho do servidor */
        try {
            return this.listener.isClosed();
        } finally {
            this.read.unlock();
        }
    }

    /* Conversão do socket server para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Socket)(Server)");
        builder.append("listener:").append(this.getAddress()).append(":").append(this.getPort()).append(";");
        builder.append("runners:");

        for (RunnerServer runner : this.runners)
            builder.append(" ").append(runner.toString());

        builder.append(";");

        return builder.toString();
    }
}
