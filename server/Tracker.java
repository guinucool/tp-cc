package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import server.Node.NodeException;

public class Tracker {
    
    private Map<String,Node> nodes;

    public Tracker() {
        this.nodes = new HashMap<String, Node>();
    }

    public void addNode(Node node) throws NodeException {
        if (this.nodes.containsKey(node.getIp()))
            throw new NodeException("Node already exists (" + node.getIp() + ")");

        this.nodes.put(node.getIp(), node);
    }

    public void removeNode(String ip) throws NodeException, IOException {
        if (!this.nodes.containsKey(ip))
            throw new NodeException("Node doesn't exist (" + ip + ")");

        this.nodes.remove(ip).closeSocket();
    }

    public void removeAllNodes() throws IOException {
        for (Node n : this.nodes.values()) {
            n.closeNode();
        }
    }
}
