package model.tracker;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import model.file.FileException;
import model.file.TrackerFile;
import model.file.block.BlockException;
import model.node.Node;
import model.tracker.TrackerException.FileNotInTrackerException;
import model.tracker.TrackerException.FilenameInTrackerException;

/**
 * Objeto que define como o tracker funciona e armazena informação.
 * 
 * @param tracker
 *      Singleton que define o tracker de um servidor.
 * 
 * @param files
 *      Os ficheiros que existem no sistema.
 * 
 * @param nodes
 *      Os nodes que estão ligados ao servidor.
 */
public class Tracker {

    private static Tracker tracker;

    private Map<String,TrackerFile> files;
    private Map<String,Node> nodes;

    /* Locks */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock reader = this.lock.readLock();
    private Lock writer = this.lock.writeLock();

    /* Constructors */
    private Tracker() {
        this.files = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public static Tracker getInstance() {
        if (tracker == null)
            tracker = new Tracker();

        return tracker;
    }

    /* Setters */
    public void addNode(Node node) throws TrackerException {

        this.writer.lock();

        try {
            if (this.nodes.containsKey(node.getIp()))
                throw new TrackerException("node-defined");

            this.nodes.put(node.getIp(),(Node) node.clone());
        } finally {
            this.writer.unlock();
        }
    }

    public void removeNode(String ip) throws TrackerException {

        this.writer.lock();

        try {
            if (!this.nodes.containsKey(ip))
                throw new TrackerException("node-undefined");

            Node node = this.nodes.remove(ip);

            for (TrackerFile file : this.files.values())
                file.removeNode(node);
        } finally {
            this.writer.unlock();
        }
    }

    public void addFile(TrackerFile file) throws FilenameInTrackerException {
        
        this.writer.lock();

        try {
            if (this.hasRepeatedFile(file))
                throw new FilenameInTrackerException(file.getName());

            if (!this.files.containsKey(file.getName()))
                this.files.put(file.getName(), (TrackerFile) file.clone());
        } finally {
            this.writer.unlock();
        }
    }

    public void removeFile(String filename) throws TrackerException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            this.files.remove(filename);
        } finally {
            this.writer.unlock();
        }
    }

    /* Getters */
    public List<Node> getNodes() {
        this.reader.lock();

        try {
            List<Node> nodelist = new ArrayList<>();

            for (Node node : this.nodes.values())
                nodelist.add((Node) node.clone());

            return nodelist;
        } finally {
            this.reader.unlock();
        }
    }

    public Node getNode(String ip) throws TrackerException {
        this.reader.lock();

        try {    
            if (!this.nodes.containsKey(ip))
                throw new TrackerException("node-undefined");

            return (Node) this.nodes.get(ip).clone();
        } finally {
            this.reader.unlock();
        }
    }

    public List<TrackerFile> getFiles() {
        this.reader.lock();

        try {
            List<TrackerFile> filelist = new ArrayList<>();

            for (TrackerFile file : this.files.values())
                filelist.add((TrackerFile) file.clone());

            return filelist;
        } finally {
            this.reader.unlock();
        }
    }

    public TrackerFile getFile(String filename) throws FileNotInTrackerException {
        this.reader.lock();

        try {
            if(!this.files.containsKey(filename))
                throw new FileNotInTrackerException(filename);

            return (TrackerFile) this.files.get(filename).clone();
        } finally {
            this.reader.unlock();
        }
    }

    /* Checkers */
    private boolean hasRepeatedFile(TrackerFile file) {
        TrackerFile tfile = this.files.get(file.getName());

        if (tfile == null)
            return false;

        return !tfile.getHash().equals(file.getHash());
    }

    /* Auxiliar */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Tracker)files:").append(this.files.toString()).append(";");
        builder.append("nodes:").append(this.nodes.toString()).append(";");
        builder.append("lock:").append(this.lock.toString()).append(";");

        return builder.toString();
    }

    public void addNodeToFile(String filename, String ip) throws TrackerException, BlockException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            if (!this.nodes.containsKey(ip))
                throw new TrackerException("node-undefined");

            TrackerFile file = this.files.get(filename);
            Node node = this.nodes.get(ip);

            file.addNode(node);
        } finally {
            this.writer.unlock();
        }
    }

    public void addNodeToBlock(String filename, String ip, int offset) throws TrackerException, BlockException, FileException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new TrackerException("file-undefined");

            if (!this.nodes.containsKey(ip))
                throw new TrackerException("node-undefined");

            TrackerFile file = this.files.get(filename);
            Node node = this.nodes.get(ip);

            file.addNodeToBlock(offset, node);
        } finally {
            this.writer.unlock();
        }
    }
}
