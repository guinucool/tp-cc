package controller;

import java.util.*;

import files.*;
import model.file.FileException;
import model.file.TrackerFile;
import model.file.block.BlockException;
import model.node.Node;
import model.node.NodeException;
import model.tracker.Tracker;
import model.tracker.TrackerException;
import tracker.*;

public class TrackerControl {
    
    private Tracker tracker;

    /* Constructors */
    public TrackerControl(Tracker tracker) {
        this.tracker = tracker;
    }

    /* Control */
    public void registerNode(String ip, List<String> args) throws NodeException, NumberFormatException, TrackerControlException {

        if (args.size() != 1)
                throw new TrackerControlException.InvalidArgumentsException("" + args.size());

        Node node = new Node(ip, Integer.parseInt(args.get(0)));

        this.tracker.addNode(node);
    }

    public void registerFile(String ip, List<String> args) throws FileException, BlockException, TrackerControlException, TrackerException {

        if (args.size() != 3)
                throw new TrackerControlException.InvalidArgumentsException("" + args.size());

        TrackerFile file = TrackerFile.fromMessage(args);

        this.tracker.addFile(file);
        this.tracker.addNodeToFile(file.getName(), ip);
    }

    public void disconnectNode(String ip) {
        try {
            this.tracker.removeNode(ip);
        } catch (TrackerException.NodeNotInTrackerException e) {
            /* Não é preciso tratar esta exceção */
        }
    }
}
