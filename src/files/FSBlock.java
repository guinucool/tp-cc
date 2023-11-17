package files;

import java.util.*;

import tracker.*;

/**
 * Objeto que regista um bloco de um ficheiro do sistema.
 * 
 * @param offset
 *      O offset do bloco em posição (0,1,2,3,etc...).
 * 
 * @param nodes
 *      A lista de todos os nodes que possuem este bloco.
 */
public class FSBlock {
    
    private int offset;
    private List<Node> nodes;

    /* Constructors */
    public FSBlock() {
        this.offset = 0;
        this.nodes = new ArrayList<Node>();
    }

    public FSBlock(int offset) throws BlockException {
        this.setOffset(offset);
        this.nodes = new ArrayList<>();
    }

    public FSBlock(FSBlock block) {
        this.offset = block.offset;
        this.nodes = this.getNodes();
    }

    /* Setters */
    public void setOffset(int offset) throws BlockException.InvalidBlockOffsetException {
        if (offset < 0)
            throw new BlockException.InvalidBlockOffsetException("" + offset);

        this.offset = offset;
    }

    public void addNode(Node node) throws BlockException.NodeExistsBlockException {
        if (this.nodes.contains(node))
            throw new BlockException.NodeExistsBlockException(node.toString());

        this.nodes.add(node);
    }

    public void removeNode(Node node) {
        if (this.nodes.contains(node))
            this.nodes.remove(node);
    }

    /* Getters */
    public int getOffset() {
        return this.offset;
    }

    public List<Node> getNodes() {
        List<Node> nodelist = new ArrayList<Node>();

        for (Node node : this.nodes)
            nodelist.add((Node) node.clone());

        return nodelist;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        FSBlock block = (FSBlock) o;
        return this.offset == block.offset && this.nodes.equals(block.nodes);
    }

    public Object clone() {
        return new FSBlock(this);
    }

    public String toString() {
        
        StringBuilder builder =  new StringBuilder();

        builder.append("(FSBlock)offset:").append(this.offset).append(";");
        builder.append("nodes:").append(this.nodes).append(";");

        return builder.toString();
    }
}
