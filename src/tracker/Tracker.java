import java.util.*;
import java.util.concurrent.locks.*;

public class Tracker {
    private Map<String,Node> nodes;
    private Map<String,TrackerFile> files;  
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    /* Constructors */

    public Tracker() {
        this.nodes = new HashMap<>();
        this.files = new HashMap<>();
    }

    public Tracker(Map<String, Node> nodes, Map<String, TrackerFile> files) {
        this.setNodes(nodes);
        this.setFiles(files);
    }

    public Tracker(Tracker tracker) {
        this.setNodes(tracker.nodes);
        this.setFiles(tracker.files);
    }

    /* Setters */
    public void setNodes(Map<String,Node> nodes){
        this.nodes = new HashMap<String,Node>(nodes);
    }

    public void setFiles(Map<String,TrackerFile> files){
        this.files = new HashMap<String,TrackerFile>(files);
    }

    /* Getters */
    public Map<String, Node> getNodes(){
        return this.nodes;
    }

    public Map<String,TrackerFile> getFiles(){
        return this.files;
    }

    /* Métodos para manipular Nodes */
    public void addNode(Node node) {
        lock.writeLock().lock();
        try {
            this.nodes.put(node.getIp(), node);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Node getNode(String ip) {
        lock.readLock().lock();
        try {
            return this.nodes.get(ip);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removeNode(String ip) {
        lock.writeLock().lock();
        try {
            this.nodes.remove(ip);

            for (TrackerFile tf : files.values()) {
                for (FSBlock fsBlock : tf.getBlocks()) {
                    Iterator<Node> iterator = fsBlock.getNodes().iterator();
                    while (iterator.hasNext()) {
                        Node node = iterator.next();
                        if (node.getIp().equals(ip))
                            iterator.remove();
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /* Métodos para manipular TrackerFiles */
    public void addFile(TrackerFile trackerFile) {
        lock.writeLock().lock();
        try {
            this.files.put(trackerFile.getName(), trackerFile);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public TrackerFile getTrackerFile(String filename) {
        lock.readLock().lock();
        try {
            return this.files.get(filename);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removeFile(String filename) {
        lock.writeLock().lock();
        try {
            this.files.remove(filename);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
