package files;

import java.io.*;
import java.security.*;
import java.util.*;

import message.TrackMessage;

/**
 * Objeto que regista um ficheiro que um cliente possui na visão do próprio.
 * 
 * @param sent
 *      Verifica se o ficheiro do cliente já foi enviado (com sucesso) para o servidor.
 */
public class NodeFile extends FSFile {

    private static final int HASH_OFFSET = 1024;
    
    private boolean sent;

    /* Constructors */
    public NodeFile() {
        super();
        this.sent = false;
    }

    public NodeFile(File file) throws FileException {
        super(filehash(file), file.getName(), file.length());
        this.sent = false;
    }

    public NodeFile(NodeFile file) {
        super(file);
        this.sent = file.sent;
    }

    /* Setters */
    public void send() {
        this.sent = true;
    }

    /* Getters */
    public boolean isSent() {
        return sent;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        NodeFile file = (NodeFile) o;
        return super.equals(o) && this.sent == file.sent;
    }

    public Object clone() {
        return new NodeFile(this);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(NodeFile)");
        builder.append("sent:").append(this.sent).append(";");
        builder.append(super.toString());

        return builder.toString();
    }

    public TrackMessage toMessage() {
        List<String> args = new ArrayList<>(Arrays.asList(super.getHash(), super.getName(), "" + super.getSize()));

        return new TrackMessage(TrackMessage.Type.REQUEST, 200, args);
    }

    public static String filehash(File file) {

        String hash = getEmptyHash();

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

            hash = sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            /* Não é preciso tratar a exceção */
        }

        return hash;
    }
}
