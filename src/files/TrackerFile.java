import java.util.*;

public class TrackerFile {
    
    private long blocksize;
    private List<FSBlock> blocks;

    /* Constructors */
    public TrackerFile(){
        this.blocksize = 0;
        this.blocks = new ArrayList<FSBlock>();
    }

    public TrackerFile(long blocksize, List<FSBlock> blocks){
        this.setBlocksize(blocksize);
        this.setBlocks(blocks);
    }

    public TrackerFile(TrackerFile tf){
        this.setBlocksize(tf.blocksize);
        this.setBlocks(tf.blocks);
    }

    /* Setters */
    public void setBlocksize(long blocksize){
        this.blocksize = blocksize;
    }

    public void setBlocks(List<FSBlock> blocks){
        this.blocks.removeAll(this.blocks);
        this.blocks = new ArrayList<FSBlock>();
        for(FSBlock a: blocks)
            this.blocks.add(a);
    }

    /* Getters */
    public long getOffset(){
        return this.blocksize;
    }

    public List<FSBlock> getNodes(){
        ArrayList<FSBlock> res = new ArrayList<FSBlock>();
        for(FSBlock a: this.blocks)
            res.add(a);

        return res;
    }

    /* Auxiliar */

    public void addNode(FSBlock block){
        this.blocks.add(block);
    }

    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        TrackerFile tf = (TrackerFile) o;
        return this.blocksize == tf.blocksize && this.blocks.equals(tf.blocks);
    }

    public Object clone() {
        return new TrackerFile(this);
    }
}
