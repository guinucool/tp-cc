import java.util.*;
import java.util.concurrent.locks.*;

import filexcp.*;
import trackexcp.*;

public class Tracker {

    private Map<String,TrackerFile> files;
    private Map<String,Node> nodes;

    /* Locks */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock reader = this.lock.readLock();
    private Lock writer = this.lock.writeLock();

    /* Constructors */
    public Tracker() {
        this.files = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    /* Setters */
    public void addNode(Node node) {
        this.writer.lock();
        this.nodes.put(node.getIp(),(Node) node.clone());
        this.writer.unlock();
    }

    public void removeNode(String ip) throws NodeNotInTrackerException {

        this.writer.lock();

        try {
            if (!this.nodes.containsKey(ip))
                throw new NodeNotInTrackerException(ip);

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

    public void removeFile(String filename) throws FileNotInTrackerException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new FileNotInTrackerException(filename);

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

    public Node getNode(String ip) throws NodeNotInTrackerException {
        this.reader.lock();

        try {    
            if (!this.nodes.containsKey(ip))
                throw new NodeNotInTrackerException(ip);

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

    public void addNodeToFile(String filename, String ip) throws FileNotInTrackerException, NodeNotInTrackerException, NodeExistsBlockException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new FileNotInTrackerException(filename);

            if (!this.nodes.containsKey(ip))
                throw new NodeNotInTrackerException(ip);

            TrackerFile file = this.files.get(filename);
            Node node = this.nodes.get(ip);

            file.addNode(node);
        } finally {
            this.writer.unlock();
        }
    }

    public void addNodeToBlock(String filename, String ip, int offset) throws FileNotInTrackerException, NodeNotInTrackerException, NodeExistsBlockException, BlockOutOfRangeException {

        this.writer.lock();

        try {
            if (!this.files.containsKey(filename))
                throw new FileNotInTrackerException(filename);

            if (!this.nodes.containsKey(ip))
                throw new NodeNotInTrackerException(ip);

            TrackerFile file = this.files.get(filename);
            Node node = this.nodes.get(ip);

            file.addNodeToBlock(offset, node);
        } finally {
            this.writer.unlock();
        }
    }
}
