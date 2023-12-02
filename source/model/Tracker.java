package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import file.FileException;
import file.TrackFile;
import file.block.Block;
import file.block.BlockException;

/**
 * Objeto que define a estrutura do tracker e dos nodes e ficheiros que este
 * armazena.
 */
public class Tracker {
    
    private static Tracker singleton;           /* Singleton do tracker que o mantém usável para todos os nodes */

    private Map<Integer,Node> nodes;            /* Mapa de nodes para id de runner */
    private List<String> addresses;             /* Lista de endereços utilizados por nodes */
    private Map<String,TrackFile> files;        /* Mapa de ficheiro para nome de ficheiro */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();        /* Bloqueio de leitura e escrita do tracker */
    private Lock read = this.rwlock.readLock();                         /* Bloqueio de escrita do tracker */
    private Lock write = this.rwlock.writeLock();                       /* Bloqueio de escrita e leitura do tracker */

    /* Construtor vazio */
    private Tracker() {
        this.nodes = new HashMap<>();
        this.addresses = new ArrayList<>();
        this.files = new HashMap<>();
    }

    /* Devolve o singleton do tracker */
    public static Tracker getInstance() {

        /* Cria o singleton caso ele ainda não tenha sido criado */
        if (singleton == null)
            singleton = new Tracker();

        return singleton;
    }

    /* Adiciona um node ao tracker */
    public void addNode(int id, Node node) throws TrackerException {

        /* Bloqueia a escrita e leitura */
        this.write.lock();

        try {

            /* Verifica se o node não existe ainda no tracker */
            if (this.nodes.containsKey(id))
                throw new TrackerException("node-exists");

            /* Verifica se o endereço do node ainda não foi usado */
            if (this.addresses.contains(node.getAddress()))
                throw new TrackerException("address-used");

            this.nodes.put(id, (Node) node.clone());
            this.addresses.add(node.getAddress());

        } finally {
            this.write.unlock();
        }
    }

    /* Remove um node por completo do tracker */
    public void removeNode(int id) {

        /* Bloqueia a escrita e leitura */
        this.write.lock();

        /* Remove o node, caso exista */
        if (this.nodes.containsKey(id)) {
            Node node = this.nodes.remove(id);
            this.addresses.remove(node.getAddress());
        }

        this.write.unlock();
    }

    /* Adiciona um ficheiro ao tracker */
    public void addFile(int id, TrackFile file) throws TrackerException, BlockException {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Verifica se o nome do ficheiro não é usado por outro ficheiro */
            if (this.filenameUsed(file))
                throw new TrackerException("filename-used");

            /* Adiciona o ficheiro caso ainda não esteja registado */
            if (this.files.get(file.getName()) == null)
                this.files.put(file.getName(), (TrackFile) file.clone());
                
            /* Adiciona o node responsável a todos os blocos do ficheiro */
            this.addNodeToFile(id, file.getName());

        } finally {
            this.write.unlock();
        }
    }

    /* Remove um ficheiro por completo do tracker */
    public void removeFile(String filename) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Remove o ficheiro, se existir */
        if (this.files.containsKey(filename))
            this.files.remove(filename);

        this.write.unlock();
    }

    /* Adiciona um node a todos os blocos de um ficheiro */
    private void addNodeToFile(int id, String filename) throws TrackerException, BlockException {

        /* Verifica se o node existe no tracker */
        if (!this.nodes.containsKey(id))
            throw new TrackerException("node-undefined");

        /* Verifica se o ficheiro existe no tracker */
        if (!this.files.containsKey(filename))
            throw new TrackerException("file-undefined");

        /* Adiciona o node ao ficheiro */
        Node node = this.nodes.get(id);
        this.files.get(filename).addNode(node);
    }

    /* Adiciona um node a um bloco de um ficheiro */
    public void addNodeToFile(int id, String filename, int offset) throws TrackerException, FileException, BlockException {
        
        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {
                
            /* Verifica se o node existe no tracker */
            if (!this.nodes.containsKey(id))
                throw new TrackerException("node-undefined");

            /* Verifica se o ficheiro existe no tracker */
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            /* Adiciona o node ao bloco do ficheiro */
            Node node = this.nodes.get(id);
            this.files.get(filename).addNode(offset, node);

        } finally {
            this.write.unlock();
        }
    }

    /* Remove um node de todos os ficheiros em que está presente */
    public void removeNodeFromFiles(int id) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Remove o node de todos os ficheiros */
        if (this.nodes.containsKey(id)) {

            Node node = this.nodes.get(id);
            for (TrackFile file : this.files.values())
                file.removeNode(node);
        }

        this.write.unlock();
    }

    /* Devolve um node em específico do tracker */
    public Node getNode(int id) throws TrackerException {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Verifica se o node existe no tracker */
            if (!this.nodes.containsKey(id))
                throw new TrackerException("node-undefined");

            return (Node) this.nodes.get(id).clone();

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve a lista de nodes presentes no tracker */
    public List<Node> getNodes() {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Cria a lista de nodes */
            List<Node> nodes = new ArrayList<>();

            for (Node node : this.nodes.values())
                nodes.add((Node) node.clone());

            /* Devolve a lista de nodes pretendida */
            return nodes;

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve um ficheiro em específico do tracker */
    public TrackFile getFile(String filename) throws TrackerException {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Verifica se o ficheiro existe no tracker */
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            return (TrackFile) this.files.get(filename).clone();

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve a lista de ficheiros presentes no tracker */
    public List<TrackFile> getFiles() {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Cria a lista de files */
            List<TrackFile> files = new ArrayList<>();

            for (TrackFile file : this.files.values())
                files.add((TrackFile) file.clone());

            /* Devolve a lista de files pretendida */
            return files;

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve um bloco em específico de um ficheiro */
    public Block getBlock(String filename, int offset) throws TrackerException, FileException {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Verifica se o ficheiro existe no tracker */
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            return this.files.get(filename).getBlock(offset);

        } finally {
            this.read.unlock();
        }
    }

    /* Devolve a lista de blocos de um ficheiro */
    public List<Block> getBlocks(String filename) throws TrackerException {

        /* Bloqueia as operações de escrita */
        this.read.lock();

        try {

            /* Verifica se o ficheiro existe no tracker */
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            /* Devolve a lista de blocos pretendida */
            return this.files.get(filename).getBlocks();

        } finally {
            this.read.unlock();
        }
    }

    /* Verifica se existe um ficheiro (diferente) com o mesmo nome */
    private boolean filenameUsed(TrackFile file) {

        /* Procura pelo nome do ficheiro */
        TrackFile tfile = this.files.get(file.getName());

        return tfile != null && !tfile.getHash().equals(file.getHash());
    }

    /* Converte o tracker para o formato de string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Tracker)");
        builder.append("nodes: ");

        for (Map.Entry<Integer,Node> node : this.nodes.entrySet()) {
            builder.append(" ").append("key: ").append(node.getKey());
            builder.append(" ").append("value: ").append(node.getValue().toString());
        }

        builder.append(";");
        builder.append("addresses: ");

        for (String address : this.addresses)
            builder.append(" ").append(address);

        builder.append(";");
        builder.append("files: ");

        for (Map.Entry<String,TrackFile> file : this.files.entrySet()) {
            builder.append(" ").append("key: ").append(file.getKey());
            builder.append(" ").append("value: ").append(file.getValue().toString());
        }

        builder.append(";");

        return builder.toString();
    }
}
