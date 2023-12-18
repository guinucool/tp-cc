package file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import message.CommunicableException;
import message.Requestable;
import message.datagram.DatagramRequest;
import message.frame.FrameRequest;
import model.Node;
import model.NodeException;
import runner.datagram.ReliableRunner;
import runner.frame.FrameRunner;
import tools.DNS;
import tools.RequestException;
import tools.RequestManager;
import file.block.Block;
import file.block.ResponseBlock;
import file.block.TransferBlock;

/**
 * Objeto que representa um ficheiro que se pretende pedir a vários
 * nodes.
 */
public class RequestFile extends BlockFile {

    public static final int MAX_SIZE = 10000000;        /* Máximo de tamanho que pode ser pedido em simultâneo */
    
    private Map<Node,Integer> statistics;               /* Estatística que controla a receção vinda de vários nodes */
    private List<Integer> available;                    /* Lista de blocos disponíveis para pedir */
    private Map<Integer,Requestable> requests;          /* Mapa de pedidos associados a blocos */
    private Map<Node,Integer> usage;                    /* Estatística que controla o uso de cada node */
    private List<Node> disconnected;                    /* Lista que controla os nodes que não responderam */
    private Map<Node,Node> translations;                /* Mapa que armazena as traduções já efetuadas */
    private Block current;                              /* Bloco em que está a ser pedido */

    /* Construtor parametrizado */
    public RequestFile(String hash, String name, long size, int blocksize) throws FileException {
        super(hash, name, size, blocksize);
        this.setAvailable();
        this.statistics = new HashMap<>();
        this.requests = new HashMap<>();
        this.usage = new HashMap<>();
        this.disconnected = new ArrayList<>();
        this.translations = new HashMap<>();
    }

    /* Construtor de cópia */
    public RequestFile(RequestFile file) {
        super(file);
        this.setAvailable();
        this.statistics = new HashMap<>();
        this.requests = new HashMap<>();
        this.usage = new HashMap<>();
        this.disconnected = new ArrayList<>();
        this.translations = new HashMap<>();
    }

    /* Construtor binário */
    public RequestFile(byte[] data) throws FileException {
        super(data, true);
        this.setAvailable();
        this.statistics = new HashMap<>();
        this.requests = new HashMap<>();
        this.usage = new HashMap<>();
        this.disconnected = new ArrayList<>();
        this.translations = new HashMap<>();
    }

    /* Define a lista de blocos disponíveis */
    private void setAvailable() {

        /* Cria a lista que vai armazenar todos os blocos disponíveis */
        this.available = new ArrayList<>();

        /* Define todos os blocos disponíveis para download */
        for (int i = 0; i < super.getNumBlocks(); i++)
            this.available.add(i);
    }

    /* Aumenta a potuanção de um node */
    private void incrementNodeScore(Node node) {
        this.statistics.put(node, this.getNodeScore(node) + 1);
    }

    /* Aumenta o uso de um node */
    private void incrementNodeUsage(Node node) {
        this.usage.put(node, this.getNodeUsage(node) + 1);
    }

    /* Diminui o uso de um node */
    private void decrementNodeUsage(Node node) {

        /* Apenas subtrai se existerem usos do node */
        if (this.getNodeUsage(node) > 0)
            this.usage.put(node, this.getNodeUsage(node) - 1);
    }

    /* Encontra a pontuanção de um node em específico */
    private int getNodeScore(Node node) {

        if (!this.statistics.containsKey(node))
            return 0;

        return this.statistics.get(node);
    }

    /* Encontra o uso de um node em específico */
    private int getNodeUsage(Node node) {

        if (!this.usage.containsKey(node))
            return 0;

        return this.usage.get(node);
    }

    /* Calcula a performance de um node em específico */
    private int getNodePerformance(Node node) {

        /* Varíaveis auxiliares ao cálculo */
        int score = this.getNodeScore(node);
        int usage = this.getNodeUsage(node);

        if (usage == 0)
            return -1;

        if (score == 0)
            return usage;

        return usage / score;
    }

    /* Devolve o tamanho do bloco fornecido */
    private int getSizeOfBlock(int block) {

        /* Assume que o bloco é o último */
        int size = super.getLastBlocksize();

        /* Verifica se o bloco não é o último */
        if (block < super.getNumBlocks() - 1)
            size = super.getBlocksize();

        /* Devolve o tamanho */
        return size;
    }

    /* Traduz um node */
    public Node getTranslation(Node node) {

        /* Averigua se o node já foi traduzido */
        if (this.translations.containsKey(node))
            return this.translations.get(node);

        /* Caso não, executa e armazena a tradução */
        try {
            Node translation = new Node(DNS.getAddress(node.getAddress()), node.getPort());
            this.translations.put(node, translation);
            
            return translation;

        } catch (NodeException | UnknownHostException e) {
            throw new RuntimeException("node-invalid");
        }
    }

    /* Remove pedidos falhados e coloca-os de novo nos disponíveis */
    private void removeFailedRequests() throws RequestException {

        for (Map.Entry<Integer,Requestable> block : (new HashMap<>(this.requests)).entrySet()) {

            /* Remove o pedido se já estiver resolvido */
            if (block.getValue().isResolved())
                this.requests.remove(block.getKey());

            /* Obtém o bloco em si */
            DatagramRequest req = (DatagramRequest) block.getValue();

            /* Investiga se foi resolvido em falhanço */
            if (req.isFailed()) {

                /* Verifica a razão do falhanço */
                try {
                    req.exceptionThrow();
                } catch (CommunicableException e) {
                    this.available.add(block.getKey());
                    this.usage.remove(req.getNode());

                    try {

                        /* Traduz o endereço para nome antes de o associar à lista de desconectados */
                        this.disconnected.add(new Node(DNS.getName(req.getNode().getAddress()), req.getNode().getPort()));   
                    } catch (NodeException | UnknownHostException ex) {
                        throw new RuntimeException("node-invalid");
                    }
                } catch (Exception e) {
                    throw new RequestException(e.getMessage());
                }
            }
        }
    }

    /* Verifica se um bloco é compatível com o ficheiro */
    private boolean compatibleBlock(ResponseBlock block) {

        /* Verifica se o nome é compativel */
        if (!block.getFilename().equals(super.getName()))
            return false;

        /* Verifica se o tamanho é compativel */
        if (block.getSize() != super.getBlocksize() && block.getSize() != super.getLastBlocksize())
            return false;

        /* Verifica se o offset é compativel */
        if (block.getOffset() / super.getBlocksize() >= super.getNumBlocks())
            return false;

        /* Confirma o bloco */
        return true;
    }

    /* Verifica se um certo bloco foi pedido ao node fornecido */
    private boolean wasRequestedTo(int block, Node node) {

        /* Verifica se o bloco existe */
        if (!this.requests.containsKey(block))
            return false;

        return ((DatagramRequest) this.requests.get(block)).getNode().equals(node);
    }

    /* Faz um pedido de um bloco */
    private void request(int block, ReliableRunner udprunner, FrameRunner tcprunner, RequestManager manager) throws RequestException, CommunicableException, FileException {

        /* Verifica se o bloco pedido ainda existe na lista de disponíveis */
        if (!this.available.contains(block))
            throw new RuntimeException("block-undefined");

        /* Remove o bloco da lista de blocos disponíveis */
        this.available.remove(Integer.valueOf(block));

        /* Cria os argumentos do pedido */
        List<byte[]> args = new ArrayList<>(Arrays.asList(super.getName().getBytes(), ByteBuffer.allocate(4).putInt(block).array()));

        /* Cria a mensagem de pedido para informação */
        FrameRequest request = new FrameRequest((short) 203, args);
        manager.sendSequentialRequest(tcprunner, request);

        /* Variáveis auxiliarers */
        Node best = null;
        int min = Integer.MAX_VALUE;

        /* Procura pelo melhor node para download */
        for (Node node : this.current.getNodes()) {

            /* Verifica se o node não causou problemas anteriormente */
            if (!this.disconnected.contains(node)) {

                /* Traduz o node */
                Node translated = this.getTranslation(node);

                /* Calcula a performance do node em avaliação */
                int performance = this.getNodePerformance(translated);

                /* Verifica se o node é melhor que o melhor atual */
                if (min > performance) {
                    best = translated;
                    min = performance;
                }
            }
        }

        /* Verifica se o bloco é transmissível */
        if (best == null)
            throw new FileException("node-unavailable");

        /* Cria o pedido de bloco */
        TransferBlock tblock = new TransferBlock(super.getName(), block * super.getBlocksize(), this.getSizeOfBlock(block));

        /* Pede o bloco */
        DatagramRequest blockreq = new DatagramRequest((short) 100, tblock.getBytes(), best);
        this.requests.put(block, blockreq);
        this.incrementNodeUsage(best);
        manager.sendConcurrentRequest(udprunner, blockreq);
    }

    /* Faz o pedido do ficheiro */
    public void request(ReliableRunner udprunner, FrameRunner tcprunner, Condition requestWait, RequestManager manager) throws RequestException, CommunicableException, FileException {

        /* Verifica se existem pedidos ou blocos em stand by */
        while (this.requests.size() * super.getBlocksize() > MAX_SIZE || (this.available.size() == 0 && this.requests.size() != 0)) {

            /* Espera por mundanças ou pelo timeout */
            try {
                requestWait.await(5, TimeUnit.SECONDS);   
            } catch (InterruptedException e) {
                /* Não é preciso tratar a exceção */
            }

            /* Restora todos os blocos falhados à disponibilidade */
            this.removeFailedRequests();
        }

        /* Percorre todos os blocos disponíveis a pedir */
        for (Integer block : new ArrayList<>(this.available)) {
            
            /* Paragem no caso de exceção de limite */
            if (this.requests.size() * super.getBlocksize() > MAX_SIZE)
                break;

            /* Pedido de bloco */
            this.request(block, udprunner, tcprunner, manager);
        }

        /* Corre o pedido até o ficheiro estar completamente resolvido */
        if (this.requests.size() != 0 || this.available.size() != 0)
            this.request(udprunner, tcprunner, requestWait, manager);
    }

    /* Recebe blocos do ficheiro */
    public void receive(ResponseBlock block, Node node, FrameRunner tcp, RequestManager manager, String path) throws FileException, RequestException, CommunicableException {

        /* Obtém o offset em sequencial */
        int nroffset = (int) block.getOffset() / super.getBlocksize();

        /* Verifica se o bloco recebido é compatível */
        if (!this.compatibleBlock(block) || !this.wasRequestedTo(nroffset, node))
            throw new FileException("block-incompatible");

        /* Verifica se o ficheiro ainda ou já foi criado */
        if (!(new File(path + "/" + super.getName())).isFile())
            throw new FileException("file-inexistent");

        /* Escreve no ficheiro já criado */
        try {

            RandomAccessFile writer = new RandomAccessFile(path + "/" + super.getName(), "rw");

            /* Iniciar no offset */
            writer.seek(block.getOffset());

            /* Escrever informação */
            writer.write(block.getData());   
            writer.close();   

        } catch (IOException e) {
            throw new RuntimeException("file-unwritable");
        }

        /* Cria os argumentos */
        List<byte[]> args = new ArrayList<>(Arrays.asList(super.getName().getBytes(), ByteBuffer.allocate(4).putInt(nroffset).array()));

        /* Envia informação de obtenção de bloco ao tracker */
        FrameRequest request = new FrameRequest((short) 300, args);
        manager.sendSequentialRequest(tcp, request);

        /* Atualiza as utilizações e as estatísticas */
        this.decrementNodeUsage(node);
        this.incrementNodeScore(node);
    }

    /* Recebe informação acerca de um bloco do ficheiro */
    public void receive(Block block) throws FileException {

        /* Verifica se o offset do block pode pertencer ao ficheiro */
        if (block.getOffset() >= this.getNumBlocks())
            throw new FileException("block-uncompatible");

        this.current = (Block) block.clone();
    }

    /* Clona o objeto de pedido para um igual */
    public Object clone() {
        return new RequestFile(this);
    }

    /* Transforma o objeto de pedido em formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Request)").append(super.toString());
        builder.append("statistics:");

        for (Map.Entry<Node, Integer> register : this.statistics.entrySet()) {
            builder.append(" key:").append(register.getKey());
            builder.append(" value:").append(register.getValue());
        }

        builder.append(";");
        builder.append("available:");

        for (Integer block : this.available)
            builder.append(" ").append(block);

        builder.append(";");
        builder.append("requests:");

        for (Map.Entry<Integer, Requestable> register : this.requests.entrySet()) {
            builder.append(" key:").append(register.getKey());
            builder.append(" value:").append(register.getValue());
        }

        builder.append(";");
        builder.append("usage:");

        for (Map.Entry<Node, Integer> register : this.usage.entrySet()) {
            builder.append(" key:").append(register.getKey());
            builder.append(" value:").append(register.getValue());
        }

        builder.append(";");
        builder.append("disconnected:");

        for (Node node : this.disconnected)
            builder.append(" ").append(node.toString());

        builder.append(";");
        builder.append("block:").append(this.current).append(";");

        return builder.toString();
    }
}
