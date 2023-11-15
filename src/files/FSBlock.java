import java.util.*;

/**
 * Objeto que regista, na generalidade dos casos, um ficheiro do sistema.
 * 
 * @param offset
 *      O número do bloco
 * 
 * @param nodes
 *      A lista de todos os nodes que possuem o block offset
 */
public class FSBlock {
    
    private int offset;
    private List<Node> nodes;

    /* Constructors */
    public FSBlock(){
        this.offset = 0;
        this.nodes = new ArrayList<Node>();
    }

    public FSBlock(int offset,List<Node> nodes){
        this.setOffset(offset);
        this.setNodes(nodes);
    }

    public FSBlock(FSBlock fsblock){
        this.setOffset(fsblock.offset);
        this.setNodes(fsblock.nodes);
    }

    /* Setters */
    public void setOffset(int offset){
        this.offset = offset;
    }

    public void setNodes(List<Node> nodes){
        this.nodes.removeAll(this.nodes);
        this.nodes = new ArrayList<Node>();
        for(Node a: nodes)
            this.nodes.add(a);
    }

    /* Getters */
    public int getOffset(){
        return this.offset;
    }

    public List<Node> getNodes(){
        ArrayList<Node> res = new ArrayList<Node>();
        for(Node a: this.nodes)
            res.add(a);

        return res;
    }

    /* Auxiliar */

    public void addNode(Node node){
        this.nodes.add(node);
    }

    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        FSBlock fsblock = (FSBlock) o;
        return this.offset == fsblock.offset && this.nodes.equals(fsblock.nodes);
    }

    public Object clone() {
        return new FSBlock(this);
    }
}
