package model;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import file.Directory;
import file.DirectoryException;
import file.FileException;
import file.RegisterFile;
import file.RequestFile;
import file.block.Block;
import file.block.ResponseBlock;
import message.CommunicableException;
import message.frame.FrameRequest;
import runner.RunnerException;
import runner.datagram.ReliableRunner;
import runner.frame.FrameRunner;
import server.RunnerServer;
import server.ServerException;
import worker.ServerWorker;
import tools.DNS;
import tools.RequestException;
import tools.RequestManager;
import worker.DatagramWorker;
import worker.FrameWorker;
import worker.WorkerException;

/**
 * Objeto que define a estrutura e propriedades de um cliente node.
 */
public class Client {
    
    private static Client singleton;                /* Instânica singleton do cliente única */

    private Directory directory;                    /* Diretoria onde são armazenados os ficheiros do node */
    private FrameRunner tracker;                    /* Runner que liga o node ao tracker */
    private ReliableRunner transfer;                /* Runner que liga o node com nodes */
    private RequestManager manager;                 /* Gesttor de pedidos de mensagens */
    private boolean status;                         /* Status do cliente node em relação ao servidor tracker */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();        /* Lock de leitura e escrita do cliente */
    private Lock read = this.rwlock.readLock();                         /* Lock de escrita do cliente */
    private Lock write = this.rwlock.writeLock();                       /* Lock de escrita e leitura do cliente */

    private Condition requestWait = this.write.newCondition();          /* Variável de condição dos pedidos */

    /* Construtor parametrizado */
    private Client(String path, String address) throws ClientException, DirectoryException {
        this.setDirectory(path);
        this.setTracker(address, 9090);
        this.setTransfer(9090);
        this.manager = new RequestManager(requestWait);
        this.status = true;
    }

    /* Construtor parametrizado com porta */
    private Client(String path, String address, int port) throws ClientException, DirectoryException {
        this.setDirectory(path);
        this.setTracker(address, port);
        this.setTransfer(9090);
        this.manager = new RequestManager(requestWait);
        this.status = true;
    }

    /* Inicia a instância global de cliente com as propriedades fornecidas e porta 9090 */
    public static void startClient(String path, String address) throws ClientException, DirectoryException {
        if (singleton == null)
            singleton = new Client(path, address);

        /* Faz o registo do node */
        singleton.sendRegister();
    }

    /* Inicia a instância global de cliente com as propriedades fornecidas */
    public static void startClient(String path, String address, int port) throws ClientException, DirectoryException {
        if (singleton == null)
            singleton = new Client(path, address, port);

        /* Faz o registo do node */
        singleton.sendRegister();
    }

    /* Devolve a instância global de cliente */
    public static Client getInstance() {
        return singleton;
    }

    /* Define a diretoria que vai ser usada pelo cliente */
    private void setDirectory(String path) throws DirectoryException {
        this.directory = new Directory(path);
    }

    /* Define a ligação do cliente com o tracker */
    private void setTracker(String address, int port) throws ClientException {

        try {

            /* Liga ao servidor e cria um runner para a ligação */
            Socket server = new Socket(DNS.getAddress(address), port);
            this.tracker = new FrameRunner(server);
            RunnerServer listener = new RunnerServer(this.tracker, new FrameWorker(this.tracker, false));

            /* Cria a thread que irá correr o servidor */
            Thread worker = new Thread(new ServerWorker(listener));
            worker.start();

        } catch (IOException | RunnerException | ServerException | WorkerException e) {
            throw new ClientException("server-unreachable");
        }
    }

    /* Define a ligação do cliente com o vários clientes */
    private void setTransfer(int port) throws ClientException {

        try {

            /* Liga ao servidor e cria um runner para a ligação */
            this.transfer = new ReliableRunner(port);
            RunnerServer listener = new RunnerServer(this.transfer, new DatagramWorker(this.transfer));

            /* Cria a thread que irá correr o servidor */
            Thread worker = new Thread(new ServerWorker(listener));
            worker.start();

        } catch (RunnerException | ServerException | WorkerException e) {
            throw new ClientException("transfer-unreachable");
        }
    }

    /* Verifica se uma resposta foi pedida */
    public boolean wasRequested(short identifier) {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {
            
            /* Verifica se existe o tal pedido */
            return this.manager.hasRequest(identifier);

        } finally {
            this.read.unlock();
        }
    }

    /* Resolve com sucesso uma mensagem de pedido */
    public void solveRequest(short identifier) throws RequestException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Tenta resolver com sucesso um pedido */
            this.manager.solveRequest(identifier);

        } finally {
            this.write.unlock();
        }
    }

    /* Resolve em falhanço uma mensagem de pedido */
    public void failRequest(short identifier, CommunicableException e) throws RequestException {
        
        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Falha o pedido */
            this.manager.failRequest(identifier, e);

        } finally {
            this.write.unlock();
        }
    }

    /* Atualiza o estado de um ficheiro para nome repetido */
    public void updateFileStatus(String filename) throws DirectoryException {
        
        /* Bloqueia as operações de leitura e escrita */
        this.write.lock();

        try {

            /* Atualiza o estado do ficheiro */
            this.directory.updateFileStatus(filename);   

        } finally {
            this.write.unlock();
        }
    }

    /* Adiciona um pedido de ficheiro à diretoria */
    public void addRequestFile(RequestFile file) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Associa o pedido à diretoria */
        this.directory.setRequest(file);
        this.write.unlock();
    }

    /* Adiciona um bloco ao sistema */
    public void addRequestBlock(Block current) throws DirectoryException, FileException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Associa o pedido à diretoria */
        try {
            this.directory.setCurrent(current);
        } finally {
            this.write.unlock();
        }
    }

    /* Recebe um bloco de um ficheiro pedido */
    public void receiveBlock(ResponseBlock block, Node node) throws DirectoryException, FileException, RequestException, CommunicableException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Associa o pedido à diretoria */
        try {
            this.directory.receiveFile(block, node, this.tracker, this.manager);
        } finally {
            this.write.unlock();
        }
    }

    /* Devolve o caminho para a diretoria que armazena os ficheiros do node */
    public String getPath() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o caminho */
            return this.directory.getPath();

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve um ficheiro em específico, caso exista */
    public RegisterFile getFile(String filename) throws DirectoryException {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o ficheiro */
            return this.directory.getFile(filename);
            
        } finally {
            this.read.unlock();
        }
    }

    /* Devolve todos os registos de ficheiros de um node */
    public List<RegisterFile> getFiles() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o caminho */
            return this.directory.getFiles();
            
        } finally {
            this.read.unlock();
        }
    }

    /* Devolve a informação binária de um bloco */
    public byte[] getBlock(String filename, long offset, int size) throws DirectoryException {
        
        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o ficheiro */
            return this.directory.getBlock(filename, offset, size);
            
        } finally {
            this.read.unlock();
        }
    }

    /* Devolve as informações deste node sobre formato node */
    public Node getNode() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve as informações em formato de node */
            return new Node(DNS.getName(this.transfer.getAddress()), this.transfer.getPort());
            
        } catch (NodeException e) {

            /* Não é preciso tratar esta exceção */
            return null;

        } catch (UnknownHostException e) {
            throw new RuntimeException("dns-unreachable");
        } finally {
            this.read.unlock();
        }
    }

    /* Devolve o estado de relação entre cliente e tracker */
    public boolean getStatus() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o caminho */
            return this.status;
            
        } finally {
            this.read.unlock();
        }
    }

    /* Envia o registo deste node para o tracker */
    public void sendRegister() throws ClientException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Cria um node com a informação deste cliente */
            Node node = this.getNode();

            /* Cria a mensagem de pedido */
            FrameRequest request = new FrameRequest((short) 100, node.getBytes());
            this.manager.sendSequentialRequest(tracker, request);

        } catch(RequestException | CommunicableException e) {
            this.close();
            throw new ClientException("connection-closed");
        } finally {
            this.write.unlock();
        }
    }

    /* Envia o registo de ficheiros deste node para o tracker */
    public void sendFiles() throws ClientException, DirectoryException {
        
        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Envia os vários ficheiros presentes na diretoria */
            this.directory.sendFiles(manager, tracker);

        } catch(RequestException | CommunicableException e) {
            this.close();
            throw new ClientException("connection-closed");
        } finally {
            this.write.unlock();
        }
    }

    /* Envia um pediod de ficheiro para o tracker */
    public void requestFile(String filename) throws ClientException, CommunicableException, FileException, DirectoryException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Cria a mensagem de pedido */
            FrameRequest request = new FrameRequest((short) 202, filename.getBytes());
            this.manager.sendSequentialRequest(tracker, request);

            /* Começa a transferência */
            this.directory.requestFile(this.transfer, this.tracker, this.requestWait, this.manager);

        } catch(RequestException e) {
            this.close();
            throw new ClientException("connection-closed");
        } finally {
            this.write.unlock();
        }
    }

    /* Envia um pedido de desconexão ao servidor */
    public void sendDisconnect() throws ClientException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Cria a mensagem de pedido */
            FrameRequest request = new FrameRequest((short) 101);
            this.manager.sendSequentialRequest(tracker, request);

            /* Fecha o cliente */
            this.close();

        } catch(RequestException | CommunicableException e) {
            this.close();
            throw new ClientException("connection-closed");
        } finally {
            this.write.unlock();
        }
    }

    /* Fecha este cliente sem controlo de concorrência */
    private void close() {
        this.tracker.close();
        this.transfer.close();

        this.status = false;

        this.manager.failAllRequest(new RunnerException("connection-lost"));
        this.requestWait.signalAll();
    }

    /* Fecha o cliente */
    public void stop() {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        this.close();

        this.write.unlock();
    }

    /* Converte o cliente para formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Client)");
        builder.append("directory:").append(this.directory.toString()).append(";");
        builder.append("tracker:").append(this.tracker.toString()).append(";");
        builder.append("manager:").append(this.manager.toString()).append(";");
        builder.append("status:").append(this.status).append(";");

        return builder.toString();
    }
}
