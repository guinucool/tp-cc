package file.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import model.Node;
import model.NodeException;

/**
 * Objeto que define um bloco (e os nodos que o possuem) que representa parte de um ficheiro.
 */
public class Block {
    
    private int offset;             /* Offset do bloco no ficheiro */
    private List<Node> nodes;       /* Lista de nodes que possuem o bloco */

    /* Construtor parametrizado */
    public Block(int offset) {
        this.setOffset(offset);
        this.nodes = new ArrayList<>();
    }

    /* Construtor de cópia */
    public Block(Block block) {
        this.offset = block.offset;
        this.nodes = block.getNodes();
    }

    /* Construtor binário */
    public Block(byte[] data) throws BlockException, NodeException {

        this.nodes = new ArrayList<>();

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para bloco */
        try {
            this.setOffset(stream.readInt());

            int size = stream.readInt();

            for (int i = 0; i < size; i++) {

                int nsize = stream.readInt();

                byte[] node = new byte[nsize];
                stream.read(node);

                this.addNode(new Node(node));
            }

        } catch (IOException | RuntimeException e) {
            throw new BlockException("block-invalid");
        }
    }

    /* Definição do offset do bloco */
    private void setOffset(int offset) {

        /* Verifica se o offset fornecido é válido */
        if (offset < 0)
            throw new RuntimeException("offset-invalid");

        this.offset = offset;
    }

    /* Adição de um novo node ao bloco */
    public void addNode(Node node) throws BlockException {

        /* Verifica se o node já não existe no bloco */
        if (this.nodes.contains(node))
            throw new BlockException("node-defined");

        this.nodes.add(node);
    }

    /* Remoção de um node ao bloco (O node fornecido não precisa de existir no bloco) */
    public void removeNode(Node node) {
        if (this.nodes.contains(node))
            this.nodes.remove(node);
    }

    /* Devolução do offset do bloco */
    public int getOffset() {
        return this.offset;
    }

    /* Devolução da lista de nodes de um bloco */
    public List<Node> getNodes() {
        List<Node> nodelist = new ArrayList<>();

        for (Node node : this.nodes)
            nodelist.add((Node) node.clone());

        return nodelist;
    }

    /* Converte um bloco para formato binário */
    public byte[] getBytes() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.writeInt(this.offset);
            stream.writeInt(this.nodes.size());

            for (Node node : this.nodes) {

                byte[] data = node.getBytes();

                stream.writeInt(data.length);
                stream.write(data);
            }

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("block-outofmemory");
        }
    }

    /* Verifica se o bloco é igual a um objeto */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        Block block = (Block) o;
        return this.offset == block.offset && this.nodes.equals(block.nodes);
    }

    /* Clona o bloco criando um bloco idêntico */
    public Object clone() {
        return new Block(this);
    }

    /* Converte um bloco para string */
    public String toString() {
        
        StringBuilder builder =  new StringBuilder();

        builder.append("(Block)offset:").append(this.offset).append(";");
        builder.append("nodes:");

        for (Node node : this.nodes)
            builder.append(" ").append(node.toString());

        builder.append(";");

        return builder.toString();
    }
}
