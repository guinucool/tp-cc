package server;

import java.util.*;
import java.util.concurrent.locks.*;

import exception.*;

public class TrackerFile {

    private final long MaxBlockNum = 4096;
    
    private String hash; // a usar MD5 hashing (128-bit)
    private String name;
    private String ext;
    private long size;   // em bytes
    private long blockSize;  // block size em bytes
    private Map<Node,List<Integer>> nodes; // num lista de nodes para blocos
    private ReentrantReadWriteLock rwlock;
    private Lock write;
    private Lock read;

    public TrackerFile(String hash, String name, String ext, long size, Node node) {
        this.hash = hash;
        this.name = name;
        this.ext = ext;
        this.size = size;
        this.setBlock();
        this.nodes = new HashMap<>();
        this.addNodeBlock(node);
        this.rwlock = new ReentrantReadWriteLock();
        this.write = this.rwlock.writeLock();
        this.read = this.rwlock.readLock();
    }

    public TrackerFile(TrackerFile file) {
        this.hash = file.hash;
        this.name = file.name;
        this.ext = file.ext;
        this.size = file.size;
        this.blockSize = file.blockSize;
        this.nodes = file.getNodes();
        this.rwlock = new ReentrantReadWriteLock();
        this.write = this.rwlock.writeLock();
        this.read = this.rwlock.readLock();
    }

    private void addNode(Node node) {
        if (!this.nodes.containsKey(node))
            this.nodes.put(node, new ArrayList<>());
    }

    private void addBlock(Node node, int block) {
        List<Integer> blist = this.nodes.get(node);

        if (!blist.contains(block))
            blist.add(block);
    }

    public void addNodeBlock(Node node) {
        this.write.lock();
        this.addNode(node);

        for (int i = 0; i < this.getBlockNum(); i++)
            this.addBlock(node, i);
        this.write.unlock();
    }

    public void addNodeBlock(Node node, int block) throws BlockIsOutOfRangeException {
        if (block >= this.getBlockNum())
            throw new BlockIsOutOfRangeException("" + block);

        this.write.lock();
        this.addNode(node);
        this.addBlock(node, block);
        this.write.unlock();
    }

    public void removeNode(Node node) {
        this.write.lock();
        this.nodes.remove(node);
        this.write.unlock();
    }

    private void setBlock() {
	    long bsize = 1;
	    long num = Long.MAX_VALUE;

	    while(num > MaxBlockNum) {
	        num = this.size / bsize;
            bsize *= 2;
	    }

	    this.blockSize = bsize / 2;
    }

    public String getHash() {
        return this.hash;
    }

    public String getName() {
        return this.name;
    }

    public String getExt() {
        return this.ext;
    }

    public long getSize() {
        return this.size;
    }

    public long getBlockSize() {
        return this.blockSize;
    }

    private long getBlockNum() {
        long num = (this.size / this.blockSize);

        if (this.size % this.blockSize != 0)
            num++;

        return num;
    }

    public Map<Node,List<Integer>> getNodes() {
        this.read.lock();
        Map<Node,List<Integer>> map = new HashMap<>();

        for (Map.Entry<Node,List<Integer>> entry : this.nodes.entrySet())
            map.put((Node) entry.getKey().clone(), new ArrayList<>(entry.getValue()));

        this.read.unlock();
        return map;
    }

    public Object clone() {
        return new TrackerFile(this);
    }
}
