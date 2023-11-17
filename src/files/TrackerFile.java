package files;

import java.util.*;

import tracker.*;

/**
 * Objeto que define um ficheiro na visão do tracker.
 * 
 * @param MAX_BLOCK
 *      O número máximo de blocos em que um ficheiro pode ser dividido.
 * 
 * @param blocksize
 *      O tamanho de um bloco do ficheiro.
 * 
 * @param blocks
 *      A lista de blocos, e respetivos nodes que os têm, de um ficheiro.
 */
public class TrackerFile extends FSFile {

    private final int MAX_BLOCK = 4096;
    
    private long blocksize;
    private List<FSBlock> blocks;

    /* Constructors */
    public TrackerFile() {
        super();
        this.blocksize = 0;
        this.blocks = new ArrayList<FSBlock>();
    }

    public TrackerFile(String hash, String name, long size) throws FileException, BlockException {
        super(hash, name, size);
        this.generateBlocksize();
        this.blocks = new ArrayList<>();

        for (int i = 0; i < this.getNumBlocks(); i++)
            this.blocks.add(new FSBlock(i));
    }

    public TrackerFile(String hash, String name, long size, long blocksize, List<FSBlock> blocks) throws FileException {
        super(hash, name, size);
        this.setBlocksize(blocksize);
        this.setBlocks(blocks);
    }

    public TrackerFile(TrackerFile file){
        super(file);
        this.blocksize = file.blocksize;
        this.blocks = file.getBlocks();
    }

    /* Setters */
    private void setBlocksize(long blocksize) throws FileException.InvalidBlocksizeException {
        if (blocksize < 1)
            throw new FileException.InvalidBlocksizeException("" + blocksize);

        this.blocksize = blocksize;
    }

    private void setBlocks(List<FSBlock> blocks) throws FileException.InvalidBlocksException {

        if (this.getNumBlocks() != blocks.size())
            throw new FileException.InvalidBlocksException(this.blocks.toString());

        this.blocks = new ArrayList<FSBlock>();

        for(FSBlock block: blocks)
            this.blocks.add((FSBlock) block.clone());
    }

    /* Getters */
    public long getBlocksize() {
        return this.blocksize;
    }

    public long getNumBlocks() {

        long num = (this.getSize() / this.blocksize);

        if(this.getSize() % this.blocksize != 0)
            num++;

        return num;
    }

    public long getLastBlocksize() {

        long size = this.getSize() % this.blocksize;

        if (size == 0)
            size = this.getSize();

        return size;
    }

    public List<FSBlock> getBlocks() {

        List<FSBlock> blocklist = new ArrayList<FSBlock>();

        for(FSBlock block: this.blocks)
            blocklist.add((FSBlock) block.clone());

        return blocklist;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        TrackerFile file = (TrackerFile) o;
        return super.equals(o) && this.blocksize == file.blocksize && this.blocks.equals(file.blocks);
    }

    public Object clone() {
        return new TrackerFile(this);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(TrackerFile)");
        builder.append("blocksize:").append(this.blocksize).append(";");
        builder.append("blocks:").append(this.blocks).append(";");
        builder.append(super.toString());

        return builder.toString();
    }

    private void generateBlocksize() {

        long blocksize = 1;
	    long num = Long.MAX_VALUE;

	    while(num > MAX_BLOCK) {
	        num = this.getSize() / blocksize;
            blocksize *= 2;
	    }

	    this.blocksize = blocksize / 2;
    }

    public void addNode(Node node) throws BlockException.NodeExistsBlockException {
        for (FSBlock block : this.blocks)
            block.addNode(node);
    }

    public void addNodeToBlock(int offset, Node node) throws BlockException.NodeExistsBlockException, FileException.BlockOutOfRangeException {
        if (offset >= this.blocks.size())
            throw new FileException.BlockOutOfRangeException("" + offset);

        this.blocks.get(offset).addNode(node);
    }

    public void removeNode(Node node) {
        for (FSBlock block : this.blocks)
            block.removeNode(node);
    }
}
