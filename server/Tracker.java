package server;

import java.util.*;
import java.util.concurrent.locks.*;

import exception.*;

public class Tracker {
    
    private Map<String,TrackerFile> files;
    private Map<String,Node> nodes;
    private ReentrantReadWriteLock rwlock;
    private Lock write;
    private Lock read;

    public Tracker() {
        this.files = new HashMap<>();
        this.nodes = new HashMap<>();
        this.rwlock = new ReentrantReadWriteLock();
        this.write = this.rwlock.writeLock();
        this.read = this.rwlock.readLock();
    }

    private Node getNode(String ip) throws NodeDoesNotExistsException {
        if (!this.nodes.containsKey(ip))
            throw new NodeDoesNotExistsException(ip);

        return this.nodes.get(ip);
    }

    public void addNode(String ip) throws NodeAlreadyExistsException {
        this.write.lock();
        try {
            if (this.nodes.containsKey(ip))
                throw new NodeAlreadyExistsException(ip);

            this.nodes.put(ip, new Node(ip));
        } finally {
            this.write.unlock();
        }
    }

    private void removeNode(String ip) {
        if (this.nodes.containsKey(ip))
            for (TrackerFile f : this.files.values())
                f.removeNode(this.nodes.get(ip));

        this.nodes.remove(ip);
    }

    public void removeSingleNode(String ip) {
        this.write.lock();
        removeNode(ip);
        this.write.unlock();
    }

    public void removeAllNodes() {
        this.write.lock();
        for (Node n : this.nodes.values())
            this.removeNode(n.getIp());
        this.write.unlock();
    }

    public TrackerFile getFile(String hash) throws FileDoesNotExistException {
        this.read.lock();
        try {
            if (!this.files.containsKey(hash))
                throw new FileDoesNotExistException(hash);

            return (TrackerFile) this.files.get(hash).clone();
        } finally {
            this.read.unlock();
        }
    }

    public void addFile(String hash, String name, String ext, String size, String ip) throws NumberFormatException, NodeDoesNotExistsException {
        long bytes = Long.parseLong(size);

        this.write.lock();
        try {
            Node node = getNode(ip);

            if (this.files.containsKey(hash)) {
                this.files.get(hash).addNodeBlock(node);
                return;
            }

            TrackerFile file = new TrackerFile(hash, name, name, bytes, node);

            this.files.put(hash, file);
        } finally {
            this.write.unlock();
        }
    }

    public void addBlock(String hash, int block, String ip) throws FileDoesNotExistException, NodeDoesNotExistsException, BlockIsOutOfRangeException {
        this.write.lock();
        try {
            if (!this.files.containsKey(hash))
                throw new FileDoesNotExistException(hash);

            TrackerFile file = this.files.get(hash);
            Node node = getNode(ip);

            file.addNodeBlock(node, block);
        } finally {
            this.write.unlock();
        }
    }
}
