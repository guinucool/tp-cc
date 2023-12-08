package file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Objeto que define a estrutura de um ficheiro que pode ser divido em blocos
 * de x bytes.
 */
public abstract class BlockFile extends FSFile {
    
    public final int MAX_BLOCK = 8192;          /* Número máximo de blocos possível */
    public final int MAX_SIZE = 262144;         /* Tamanho máximo de bloco possível */

    private int blocksize;          /* Tamanho de bloco definido para o ficheiro */

    /* Construtor vazio */
    public BlockFile() {
        super();
        this.blocksize = 0;
    }

    /* Construtor parametrizado */
    public BlockFile(String hash, String name, long size) throws FileException {
        super(hash, name, size);
        this.setBlocksize();
    }

    /* Construtor parametrizado com blocksize pré-definido */
    public BlockFile(String hash, String name, long size, int blocksize) throws FileException {
        super(hash, name, size);
        this.setBlocksize(blocksize);
    }

    /* Construtor de cópia */
    public BlockFile(BlockFile file){
        super(file);
        this.blocksize = file.blocksize;
    }

    /* Construtor binário */
    public BlockFile(byte[] data, boolean blocksize) throws FileException {
        /* Constroi o file primeiro */
        super(data);

        /* Vai buscar o blocksize, se existir */
        if (blocksize) {

            ByteArrayInputStream barray = new ByteArrayInputStream(data);
            DataInputStream stream = new DataInputStream(barray);

            /* Conversão binária para node */
            try {
                stream.readUTF();
                stream.readUTF();
                stream.readLong();
                this.setBlocksize(stream.readInt());

            } catch (IOException e) {
                throw new FileException("file-invalid");
            }

        } else {
            this.setBlocksize();
        }
    }

    /* Define qual vai ser o tamanho dos blocos dependendo das propriedades do ficheiro */
    private void setBlocksize() {

        /* Define o número máximo de blocos para o ficheiro */
        int max = MAX_BLOCK;
        while (super.getSize() < max * 50 && max > 8)
            max /= 2;

        /* Encontra o tamanho de bloco mais eficiente */
        int blocksize = 1;
	    long num = Long.MAX_VALUE;

	    while(num > max && blocksize < MAX_SIZE * 2) {

	        num = this.getSize() / blocksize;

            if (this.getSize() % blocksize != 0)
                num++;

            blocksize *= 2;
	    }

	    this.blocksize = blocksize / 2;
    }

    /* Define o tamanho de um bloco do ficheiro com um valor já pré-definido */
    private void setBlocksize(int blocksize) throws FileException {

        /* Descobre o máximo para este ficheiro */
        int max = this.getMaxBlocks();

        if (super.getSize() % blocksize != 0)
            max--;

        /* Verifica se o blocksize fornecido é válido */
        if (blocksize < 1 || super.getSize() / blocksize > max)
            throw new FileException("blocksize-invalid");

        this.blocksize = blocksize;
    }

    /* Devolve o tamanho definido para blocos deste ficheiro */
    public int getBlocksize() {
        return this.blocksize;
    }

    /* Devolve o tamanho do último bloco do ficheiro (pode ser igual aos outros) */
    public int getLastBlocksize() {

        int size = (int) super.getSize() % this.blocksize;

        if (size == 0)
            size = this.getBlocksize();

        return size;
    }

    /* Calcula o número de blocos que um ficheiro tem */
    public int getNumBlocks() {

        int num = (int) (super.getSize() / this.blocksize);

        if(super.getSize() % this.blocksize != 0)
            num++;

        return num;
    }

    /* Calcula o número máximo de blocos que um ficheiro pode ter */
    public int getMaxBlocks() {

        /* Define o número máximo de blocos para o ficheiro */
        int max = MAX_BLOCK;
        while (super.getSize() < max * 50 && max > 8)
            max /= 2;

        return max;
    }

    /* Converte o ficheiro em formato binário */
    public byte[] getBytes() throws FileException {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.write(super.getBytes());
            stream.writeInt(this.blocksize);

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new FileException("file-outofmemory");
        }
    }

    /* Verifica se um objeto é igual ao ficheiro */
    public boolean equals(Object o) {

        BlockFile file = (BlockFile) o;
        return super.equals(o) && this.blocksize == file.blocksize;
    }

    /* Converte o ficheiro para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Block)").append(super.toString());
        builder.append("blocksize:").append(this.blocksize).append(";");

        return builder.toString();
    }
}
