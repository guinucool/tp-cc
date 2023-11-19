package controller;

import model.file.FileException;
import model.file.TrackerFile;
import model.file.block.BlockException;
import model.node.Node;
import model.node.NodeException;
import model.tracker.Tracker;
import model.tracker.TrackerException;


public class TrackerControl {
    
    private Tracker tracker;

    /* Constructors */
    public TrackerControl() {
        this.tracker = Tracker.getInstance();
    }

    /* Control */
    public void registerNode(String ip, String port) throws NodeException, TrackerException {
        Node node = Node.fromStrings(ip, port);
        this.tracker.addNode(node);
    }

    public void registerFile(String ip, String hash, String filename, String size) throws FileException, BlockException, TrackerException {

        TrackerFile file = TrackerFile.fromStrings(hash, filename, size);

        this.tracker.addFile(file);
        this.tracker.addNodeToFile(file.getName(), ip);
    }

    public void disconnectNode(String ip) {
        try {
            this.tracker.removeNode(ip);
        } catch (TrackerException e) {
            /* Não é preciso tratar esta exceção */
        }
    }
}
