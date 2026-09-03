package file.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Objeto que define um pedido de bloco.
 */
public class TransferBlock {
    
    private String filename;            /* Nome do ficheiro ao qual o pedido pertence */
    private long offset;                /* Offset do bloco pretendido */
    private int size;                   /* Tamanho do bloco pretendido */

    /* Construtor parametrizado */
    public TransferBlock(String filename, int offset, int size) {
        this.setFilename(filename);
        this.setOffset(offset);
        this.setSize(size);
    }

    /* Construtor de cópia */
    public TransferBlock(TransferBlock block) {
        this.filename = block.filename;
        this.offset = block.offset;
        this.size = block.size;
    }

    /* Construtor binário */
    public TransferBlock(byte[] data) throws BlockException {

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para pedido de bloco */
        try {
            this.setFilename(stream.readUTF());
            this.setOffset(stream.readLong());
            this.setSize(stream.readInt());

        } catch (IOException | RuntimeException e) {
            throw new BlockException("transfer-invalid");
        }
    }

    /* Define o filename do bloco pedido */
    private void setFilename(String filename) {

        /* Verifica se o filename fornecido é válido */
        if (filename.equals(""))
            throw new RuntimeException("filename-invalid");

        this.filename = filename;
    }

    /* Define o offset do bloco pedido */
    private void setOffset(long offset) {

        /* Verifica se o offset fornecido é válido */
        if (offset < 0)
            throw new RuntimeException("offset-invalid");

        this.offset = offset;
    }

    /* Define o tamanho do bloco pedido */
    private void setSize(int size) {

        /* Verifica se o tamanho fornecido é válido */
        if (size < 0)
            throw new RuntimeException("size-invalid");

        this.size = size;
    }

    /* Devolve o nome do ficheiro */
    public String getFilename() {
        return this.filename;
    }

    /* Devolve o offset do bloco no ficheiro */
    public long getOffset() {
        return this.offset;
    }

    /* Devolve o tamanho do bloco pedido */
    public int getSize() {
        return this.size;
    }

    /* Converte o pedido de bloco em formato binário */
    public byte[] getBytes() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.writeUTF(this.filename);
            stream.writeLong(this.offset);
            stream.writeInt(this.size);

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("transfer-outofmemory");
        }
    }

    /* Compara dois pedidos de blocos para verificar se são idênticos */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        TransferBlock block = (TransferBlock) o;
        return this.filename.equals(block.filename) && this.offset == block.offset && this.size == block.size;
    }

    /* Clona o pedido de bloco num idêntico */
    public Object clone() {
        return new TransferBlock(this);
    }

    /* Converte um pedido de bloco em formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(TransferBlock)");
        builder.append("filename:").append(this.filename).append(";");
        builder.append("offset:").append(this.offset).append(";");
        builder.append("size:").append(this.size).append(";");

        return builder.toString();
    }
}
