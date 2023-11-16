import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Tracker {
    private Map<String,Node> nodes;
    private Map<String,TrackerFile> files;  
    private final ReentrantLock lock;

    /* Constructors */

    public Tracker() {
        this.nodes = new HashMap<>();
        this.files = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public Tracker(Map<String, Node> nodes, Map<String, TrackerFile> files) {
        this.setNodes(nodes);
        this.setFiles(files);
        this.lock = new ReentrantLock();
    }

    public Tracker(Tracker tracker) {
        this.setNodes(tracker.nodes);
        this.setFiles(tracker.files);
        this.lock = new ReentrantLock();
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
        lock.lock();
        try {
            this.nodes.put(node.getIp(), node);
        } finally {
            lock.unlock();
        }
    }

    public Node getNode(String ip) {
        lock.lock();
        try {
            return this.nodes.get(ip);
        } finally {
            lock.unlock();
        }
    }

    public void removeNode(String ip) {
        lock.lock();
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
            lock.unlock();
        }
    }

    /* Métodos para manipular TrackerFiles */
    public void addFile(TrackerFile trackerFile) {
        lock.lock();
        try {
            this.files.put(trackerFile.getName(), trackerFile);
        } finally {
            lock.unlock();
        }
    }

    public TrackerFile getTrackerFile(String filename) {
        lock.lock();
        try {
            return this.files.get(filename);
        } finally {
            lock.unlock();
        }
    }

    public void removeFile(String filename) {
        lock.lock();
        try {
            this.files.remove(filename);
        } finally {
            lock.unlock();
        }
    }
}
