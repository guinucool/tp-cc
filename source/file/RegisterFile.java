package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Objeto que define a estrutura de um ficheiro na visão de um node.
 */
public class RegisterFile extends FSFile {

    private static int HASH_OFFSET = 1024;          /* Offset do algoritmo de md5hash */
    
    private int status;         /* Status do ficheiro em relação ao servidor (0 - enviado, 1 - não enviado, 2 - nome repetido) */

    /* Construtor vazio */
    public RegisterFile() {
        super();
        this.status = 1;
    }

    /* Construtor parametrizado */
    public RegisterFile(String hash, String name, long size) throws FileException {
        super(hash, name, size);
        this.status = 1;
    }

    /* Construtor de ficheiro */
    public RegisterFile(File file) throws FileException {

        /* Constroi o ficheiro em si */
        super(md5hash(file), file.getName(), file.length());

        /* Verifica se o ficheiro fornecido é um ficheiro */
        if (!file.isFile())
            throw new FileException("file-invalid");

        this.status = 1;
    }

    /* Construtor de cópia */
    public RegisterFile(RegisterFile file) {
        super(file);
        this.status = file.status;
    }

    /* Definição do status do ficheiro */
    public void setStatus(int status) {
        this.status = status;
    }

    /* Devolução do status do ficheiro */
    public int getStatus() {
        return this.status;
    }

    /* Verifica se um ficheiro não é igual a um objeto */
    public boolean equals(Object o) {

        RegisterFile file = (RegisterFile) o;
        return super.equals(o) && this.status == file.status;
    }

    /* Clona um ficheiro criando um ficheiro idêntico */
    public Object clone() {
        return new RegisterFile(this);
    }

    /* Converte o ficheiro em formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Register)").append(super.toString());
        builder.append("status: ").append(this.status).append(";");

        return builder.toString();
    }

    /* Recebendo um ficheiro, cria uma hash md5 única para o mesmo */
    public static String md5hash(File file) throws FileException {

        try {
            MessageDigest digestor = MessageDigest.getInstance("MD5");
            FileInputStream reader = new FileInputStream(file);

            byte[] bytes = new byte[HASH_OFFSET];

            while (reader.read(bytes) != -1)
                digestor.update(bytes);

            reader.close();

            byte[] dbytes = digestor.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : dbytes)
                sb.append(String.format("%02x", b & 0xff));

            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new FileException("file-invalid");
        }
    }
}
