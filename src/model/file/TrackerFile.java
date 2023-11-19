package model.file;

import java.util.List;
import java.util.ArrayList;

import model.file.block.BlockException;
import model.file.block.FSBlock;
import model.node.Node;

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

    public TrackerFile(String hash, String name, long size) throws FileException {
        super(hash, name, size);
        this.generateBlocksize();
        this.blocks = new ArrayList<>();

        for (int i = 0; i < this.getNumBlocks(); i++) {
            try {
                this.blocks.add(new FSBlock(i));
            } catch (BlockException e) {
                /* Não é preciso tratar esta exceção */
            }
        }
    }

    /*public TrackerFile(String hash, String name, long size, long blocksize, List<FSBlock> blocks) throws FileException {
        super(hash, name, size);
        this.setBlocksize(blocksize);
        this.setBlocks(blocks);
    }*/

    public TrackerFile(TrackerFile file){
        super(file);
        this.blocksize = file.blocksize;
        this.blocks = file.getBlocks();
    }

    /* Setters */
    /*private void setBlocksize(long blocksize) throws FileException {
        if (blocksize < 1)
            throw new FileException("invalid blocksize parameter");

        this.blocksize = blocksize;
    }

    private void setBlocks(List<FSBlock> blocks) throws FileException {

        if (this.getNumBlocks() != blocks.size())
            throw new FileException("invalid blocks parameter");

        this.blocks = new ArrayList<FSBlock>();

        for(FSBlock block: blocks)
            this.blocks.add((FSBlock) block.clone());
    }*/

    /* Getters */
    public long getBlocksize() {
        return this.blocksize;
    }

    public long getNumBlocks() {

        long num = (super.getSize() / this.blocksize);

        if(super.getSize() % this.blocksize != 0)
            num++;

        return num;
    }

    /*public long getLastBlocksize() {

        long size = super.getSize() % this.blocksize;

        if (size == 0)
            size = super.getSize();

        return size;
    }*/

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

    /*public TrackMessage toMessage() {
        
        List<String> args = new ArrayList<>(Arrays.asList(super.getHash(), super.getName(), "" + super.getSize(), "" + this.getBlocksize()));

        for (FSBlock block : this.blocks)
            args.add(block.toMessage());

        return new TrackMessage(TrackMessage.Type.RESPONSE, 200, args);
    }*/

    private void generateBlocksize() {

        long blocksize = 1;
	    long num = Long.MAX_VALUE;

	    while(num > MAX_BLOCK) {
	        num = this.getSize() / blocksize;
            blocksize *= 2;
	    }

	    this.blocksize = blocksize / 2;
    }

    public void addNode(Node node) throws BlockException {
        for (FSBlock block : this.blocks)
            block.addNode(node);
    }

    public void addNodeToBlock(int offset, Node node) throws BlockException, FileException {
        if (offset >= this.blocks.size() || offset < 0)
            throw new FileException("offset-invalid");

        this.blocks.get(offset).addNode(node);
    }

    public void removeNode(Node node) {
        for (FSBlock block : this.blocks)
            block.removeNode(node);
    }

    public List<String> toStrings() {

        List<String> list = super.toStrings();
        list.add(blocksize + "");

        StringBuilder builder = new StringBuilder();
        String prefix = "";

        for (FSBlock block : this.blocks) {
            List<String> blocklist = block.toStrings();
            builder.append(prefix).append(blocklist.get(0)).append(":");
            builder.append(String.join(",", blocklist.subList(1, blocklist.size() - 1)));

            prefix = ";";
        }

        list.add(builder.toString());

        return list;
    }

    public static TrackerFile fromStrings(String hash, String filename, String size) throws FileException {

        try {
            long bytesize = Long.parseLong(size);
            return new TrackerFile(hash, filename, bytesize);

        } catch (NumberFormatException e) {
            throw new FileException("size-invalid");
        }
    }
}
