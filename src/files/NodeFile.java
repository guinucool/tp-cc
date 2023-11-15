import filexcp.*;

/**
 * Objeto que regista um ficheiro que um cliente possui na visão do próprio.
 * 
 * @param sent
 *      Verifica se o ficheiro do cliente já foi enviado (com sucesso) para o servidor.
 */
public class NodeFile extends FSFile {
    
    private boolean sent;

    /* Constructors */
    public NodeFile() {
        super();
        this.sent = false;
    }

    public NodeFile(String hash, String name, String extension, long size) throws FileException {
        super(hash, name, extension, size);
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
}
