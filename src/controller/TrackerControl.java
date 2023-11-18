package controller;

import files.*;
import tracker.*;
import tracker.TrackerException.*;

public class TrackerControl {
    
    private Tracker tracker;

    /* Constructors */
    public TrackerControl(Tracker tracker) {
        this.tracker = tracker;
    }

    /* Control */
    public void registerNode(String ip, int port) throws NodeException {
        Node newNode = new Node(ip,port);
        tracker.addNode(newNode);

        System.out.println("Node registered successfully.");
    }

    public void registerFile(TrackerFile file) {
        try {
            tracker.addFile(file);
            System.out.println("File registered successfully.");
        } catch (FilenameInTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void disconnectNode(String ip) {
        try {
            tracker.removeNode(ip);
            System.out.println("Node disconnected successfully.");
        } catch (NodeNotInTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
