package file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import file.block.Block;
import file.block.BlockException;
import model.Node;

/**
 * Objeto que define a estrutura do registo de um ficheiro no tracker.
 */
public class TrackFile extends BlockFile {

    private Map<Integer,Block> blocks;          /* Mapa que armazena blocos e os nodes associados */

    /* Construtor vazio */
    public TrackFile() {
        super();
        this.blocks = new HashMap<>();
    }

    /* Construtor parametrizado */
    public TrackFile(String hash, String name, long size) throws FileException {
        super(hash, name, size);
        this.setBlocks();
    }

    /* Construtor de cópia */
    public TrackFile(TrackFile file) {
        super(file);
        this.blocks = new HashMap<>();

        /* Popula o novo hash map */
        for (Block block : file.getBlocks())
            this.blocks.put(block.getOffset(), block);
    }

    /* Construtor binário */
    public TrackFile(byte[] data) throws FileException {
        super(data, false);
        this.setBlocks();
    }

    /* Define um mapa de blocos que não têm nenhum node associdado */
    private void setBlocks() {

        /* Cria o hash map que vai armazenar os blocos */
        this.blocks = new HashMap<>();

        /* Descobre a quantidade de blocos */
        int size = this.getNumBlocks();

        /* Vai criando e armazenando os vários blocos */
        for (int i = 0; i < size; i++) {
            try {
                this.blocks.put(i, new Block(i));
            } catch (BlockException e) {
                /* Não é preciso tratar esta exceção */
            }
        }
    }

    /* Adiciona um nodo a todos os blocos do ficheiro */
    public void addNode(Node node) throws BlockException {
        for (Block block : this.blocks.values())
            block.addNode(node);
    }

    /* Adiciona um nodo a um bloco em específico do ficheiro */
    public void addNode(int offset, Node node) throws BlockException, FileException {
        if (offset >= this.blocks.size() || offset < 0)
            throw new FileException("offset-invalid");

        this.blocks.get(offset).addNode(node);
    }

    /* Remove um nodo de todos os blocos do ficheiro */
    public void removeNode(Node node) {
        for (Block block : this.blocks.values())
            block.removeNode(node);
    }

    /* Devolve o número de blocos que um ficheiro tem */
    public int getNumBlocks() {

        /* Caso a lista blocos já tenha sido gerada */
        if (this.blocks != null && this.blocks.size() != 0)
            return this.blocks.size();

        /* Caso contrário calcula o tamanho */
        return super.getNumBlocks();
    }

    /* Devolve um bloco em específico do ficheiro */
    public Block getBlock(int offset) throws FileException {

        if (offset >= this.blocks.size() || offset < 0)
            throw new FileException("offset-invalid");

        return (Block) this.blocks.get(offset).clone();
    }

    /* Devolve a lista de blocos que um ficheiro tem */
    public List<Block> getBlocks() {

        List<Block> blocklist = new ArrayList<Block>();

        for (Block block: this.blocks.values())
            blocklist.add((Block) block.clone());

        return blocklist;
    }

    /* Verifica se um objeto é igual ao ficheiro track */
    public boolean equals(Object o) {

        TrackFile file = (TrackFile) o;
        return super.equals(o) && this.blocks.equals(file.blocks);
    }

    /* Clona o ficheiro criando um novo idêntico */
    public Object clone() {
        return new TrackFile(this);
    }

    /* Converte o ficheiro para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Track)").append(super.toString());
        builder.append("blocks:");

        for (Map.Entry<Integer,Block> block : this.blocks.entrySet()) {
            builder.append(" ").append("key: ").append(block.getKey());
            builder.append(" ").append("value: ").append(block.getValue().toString());
        }

        builder.append(";");

        return builder.toString();
    }
}