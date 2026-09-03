package file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Objeto que define, na generalidade, a representação de um ficheiro que
 * existe fisicamente.
 */
public class FSFile {
    
    public static final String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";          /* Hash de um ficheiro inválido ou vazio */

    private String hash;            /* Hash MD5 que define um ficheiro específico */
    private String name;            /* Nome do ficheiro */
    private long size;              /* Tamanho do ficheiro */
    
    /* Constructor vazio */
    public FSFile() {
        this.hash = EMPTY_MD5;
        this.name = "";
        this.size = 0;
    }

    /* Construtor parametrizado */
    public FSFile(String hash, String name, long size) throws FileException {
        this.setHash(hash);
        this.setName(name);
        this.setSize(size);
    }

    /* Construtor de cópia */
    public FSFile(FSFile file) {
        this.hash = file.hash;
        this.name = file.name;
        this.size = file.size;
    }

    /* Construtor binário */
    public FSFile(byte[] data) throws FileException {

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para node */
        try {
            this.setHash(stream.readUTF());
            this.setName(stream.readUTF());
            this.setSize(stream.readLong());

        } catch (IOException e) {
            throw new FileException("file-invalid");
        }
    }

    /* Define qual é a hash do ficheiro que é representado */
    private void setHash(String hash) throws FileException {

        /* Verifica se a hash fornecida é válida */
        if (hash.equals(EMPTY_MD5) || hash.equals(""))
            throw new FileException("hash-invalid");

        this.hash = hash;
    }

    /* Define qual é o nome do ficheiro que é representado */
    public void setName(String name) throws FileException {

        /* Verifica se o filename fornecido é válido */
        if (name.equals(""))
            throw new FileException("filename-invalid");

        this.name = name;
    }

    /* Define o tamanho do ficheiro que é representado */
    public void setSize(long size) throws FileException {

        /* Verifica se o tamanho fornecido é válido */
        if (size <= 0)
            throw new FileException("size-invalid");

        this.size = size;
    }

    /* Devolve a hash única do ficheiro */
    public String getHash(){
        return this.hash;
    }

    /* Devolve o nome do ficheiro */
    public String getName(){
        return this.name;
    }

    /* Devolve o tamanho do ficheiro */
    public long getSize(){
        return this.size;
    }

    /* Converte o ficheiro em formato binário */
    public byte[] getBytes() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.writeUTF(this.hash);
            stream.writeUTF(this.name);
            stream.writeLong(this.size);

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("file-outofmemory");
        }
    }

    /* Verifica se um objeto é igual ao ficheiro */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        FSFile file = (FSFile) o;
        return this.hash.equals(file.hash) && this.name.equals(file.name) && this.size == file.size;
    }

    /* Clona um ficheiro para outro idêntico */
    public Object clone() {
        return new FSFile(this);
    }

    /* Converte o ficheiro num formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(FSFile)hash:").append(this.hash).append(";");
        builder.append("name:").append(this.name).append(";");
        builder.append("size:").append(this.size).append(";");

        return builder.toString();
    }
}
