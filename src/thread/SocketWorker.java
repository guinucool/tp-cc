package thread;

import java.io.*;
import java.util.*;

import controller.*;
import message.*;
import model.file.FileException;
import model.file.block.BlockException;
import model.node.NodeException;
import model.tracker.TrackerException;
import socket.*;
import tracker.*;
import view.*;

public class SocketWorker implements Runnable {
    
    private SocketRunner runner;
    private TrackerControl control;
    private TrackerView view;

    /* Constructors */
    public SocketWorker(SocketRunner runner) {
        this.runner = runner;
    }

    /* Thread */
    public void run() {
        while (!this.runner.isClosed()) {
            try {
                try {
                    TrackMessage msg = this.runner.listenMessage();

                    if (msg.getType() != TrackMessage.Type.REQUEST)
                        break;

                    if (msg.getCode() == 100) {
                        this.control.registerNode(this.runner.getDestIP(), msg.getArguments());
                        this.runner.sendMessage(new TrackMessage(TrackMessage.Type.RESPONSE, 0, Arrays.asList()));
                    }
                    else if (msg.getCode() == 101) {
                        this.runner.sendMessage(new TrackMessage(TrackMessage.Type.RESPONSE, 0, Arrays.asList()));
                        break;
                    }
                    else if (msg.getCode() == 200) {
                        this.control.registerFile(this.runner.getDestIP(), msg.getArguments());
                        this.runner.sendMessage(new TrackMessage(TrackMessage.Type.RESPONSE, 0, Arrays.asList()));
                    }
                    else
                        break;


                } catch (TrackerException.FileNotInTrackerException e) {
                    this.runner.sendMessage(new TrackMessage(TrackMessage.Type.RESPONSE, 202, Arrays.asList(e.getMessage())));
                } catch (TrackerException.FilenameInTrackerException e) {
                    this.runner.sendMessage(new TrackMessage(TrackMessage.Type.RESPONSE, 201, Arrays.asList(e.getMessage())));
                } catch (NodeException | NumberFormatException | TrackerControlException | FileException | TrackerException | BlockException e) {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }


        this.control.disconnectNode(this.runner.getDestIP());
        this.runner.close();
    }
}
