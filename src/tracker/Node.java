package tracker;

import java.util.*;

/**
 * Objeto que regista um node que possui ligação com o servidor.
 * 
 * @param ip
 *      O ip associado ao node.
 * 
 * @param port
 *      A porta associada ao servidor de distribuição de ficheiros do node.
 */
public class Node {

    private static List<String> iplist = new ArrayList<>();
    
    private String ip;
    private int port;

    /* Constructors */
    public Node() {
        this.ip = "";
        this.port = 0;
    }

    public Node(String ip, int port) throws NodeException {
        this.setIp(ip);
        this.setPort(port);
    }

    public Node(Node node) {
        this.ip = node.ip;
        this.port = node.port;
    }

    /* Setters */
    private void setIp(String ip) throws NodeException.NodeRegisteredException, NodeException.EmptyNodeIpException {
        if (iplist.contains(ip))
            throw new NodeException.NodeRegisteredException(ip);

        if (ip.equals(""))
            throw new NodeException.EmptyNodeIpException(ip);

        this.ip = ip;
    }

    public void setPort(int port) throws NodeException.InvalidNodePortException {
        if (port < 0 || port > 65535)
            throw new NodeException.InvalidNodePortException("" + port);

        this.port = port;
    }

    /* Getters */
    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        Node node = (Node) o;
        return this.ip.equals(node.ip) && this.port == node.port;
    }

    public Object clone() {
        return new Node(this);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Node)ip:").append(this.ip).append(";");
        builder.append("port:").append(this.port).append(";");

        return builder.toString();
    }

    public String toMessage() {

        StringBuilder builder = new StringBuilder();

        builder.append(this.ip).append(":").append(this.port);

        return builder.toString();
    }
}
