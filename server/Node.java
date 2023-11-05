/* Pacote de objetos do servidor (Tracker) */
package server;

/**
 * @class Node
 * 
 * Representação de um node na visão do servidor (Tracker).
 * 
 * @param ip IP do node.
 */
public class Node {

    private String ip;
    
    public Node(String ip) {
        this.setIp(ip);
    }

    public Node(Node node) {
        this.ip = node.ip;
    }

    private void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    public int hashCode() {
        return this.ip.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        Node n = (Node) o;
        return this.ip.equals(n.getIp());
    }

    public Object clone() {
        return new Node(this);
    }
}
