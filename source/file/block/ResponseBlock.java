package file.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Objeto que corresponde a uma resposta de bloco.
 */
public class ResponseBlock extends TransferBlock {
    
    private byte[] data;            /* Informação binária do bloco de resposta */

    /* Construtor parametrizado */
    public ResponseBlock(TransferBlock request, byte[] data) {
        super(request);
        setData(data);
    }

    /* Construtor de cópia */
    public ResponseBlock(ResponseBlock block) {
        super(block);
        this.data = block.data.clone();
    }

    /* Construtor binário */
    public ResponseBlock(byte[] data) throws BlockException {
        super(data);

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para pedido de bloco */
        try {
            stream.readUTF();
            stream.readLong();
            stream.readInt();

            byte[] bin = new byte[this.getSize()];
            stream.read(bin);
            this.setData(bin);

        } catch (IOException | RuntimeException e) {
            throw new BlockException("response-invalid");
        }
    }

    /* Define a informação binária do bloco a ser transmitido */
    private void setData(byte[] data) {

        /* Verifica se a informação binária fornecida tem o tamanho correto */
        if (data.length != this.getSize())
            throw new RuntimeException("data-invalid");

        this.data = data.clone();
    }

    /* Devolve a informação binária do bloco recebido */
    public byte[] getData() {
        return this.data.clone();
    }

    /* Devolve a resposta de bloco em formato binário */
    public byte[] getBytes() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.write(super.getBytes());
            stream.write(this.data);

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("response-outofmemory");
        }
    }

    /* Verifica se a resposta de bloco é igual a um objeto fornecido */
    public boolean equals(Object o) {

        ResponseBlock block = (ResponseBlock) o;
        return super.equals(o) && this.data.equals(block.data);
    }

    /* Clona a resposta de bloco numa resposta idêntica */
    public Object clone() {
        return new ResponseBlock(this);
    }

    /* Converte a resposta de bloco em string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Response)").append(super.toString());
        builder.append("data:");

        for (byte b : this.data)
            builder.append(" ").append(b);

        builder.append(";");

        return builder.toString();
    }
}
