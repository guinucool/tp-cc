package thread;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import controller.TrackerControl;
import view.TrackerView;
import message.MessageException;
import message.TrackMessage;
import message.TrackMessage.Type;

import model.file.FileException;
import model.file.block.BlockException;
import model.node.NodeException;
import model.tracker.TrackerException;
import model.tracker.TrackerException.FilenameInTrackerException;

import socket.SocketRunner;
import socket.RunnerException;

public class SocketWorker implements Runnable {
    
    private SocketRunner runner;
    private TrackerControl control;
    private TrackerView view;
    private String clientIp;

    /* Constructors */
    public SocketWorker(SocketRunner runner) {
        this.runner = runner;
        this.control = new TrackerControl();
        this.view = new TrackerView();
        this.clientIp = "";
    }

    /* Thread */
    public void run() {
        try {

            this.runner.setStream();

            while (!this.runner.isClosed()) {
                TrackMessage req = this.runner.listenMessage();
                List<String> args = req.getArguments();
                List<String> res = new ArrayList<>();

                try {

                    if (req.isTarget(Type.REQUEST, 100, 2)) {
                        System.out.println("Incoming connection from " + this.runner.getDestIP() + "!");
                        this.control.registerNode(args.get(0), args.get(1));
                        this.clientIp = args.get(0);
                    }

                    else if (req.isTarget(Type.REQUEST, 200, 3)) {
                        System.out.println("Incoming file from " + this.runner.getDestIP() + "!");
                        this.control.registerFile(this.clientIp, args.get(0), args.get(1), args.get(2));
                    }

                    else if (req.isTarget(Type.REQUEST, 101, 0)) {
                        System.out.println("Incoming disconnect from " + this.runner.getDestIP() + "!");
                        this.control.disconnectNode(this.clientIp);
                    }

                    else {
                        throw new MessageException("message-invalid");
                    }

                    this.runner.sendMessage(new TrackMessage(Type.RESPONSE, 0, res));

                    if (req.isTarget(Type.REQUEST, 101, 0))
                        this.runner.close();

                } catch (FilenameInTrackerException e) {
                    this.runner.sendMessage(new TrackMessage(Type.RESPONSE, 201, Arrays.asList(e.getMessage())));
                } catch (TrackerException | NodeException | FileException | BlockException | MessageException e) {
                    this.runner.close();
                    this.control.disconnectNode(this.clientIp);
                    System.out.println("Closing unsafe connection with socket " + this.runner.getDestIP() + "!");
                }
            }

        } catch (RunnerException e) {
            this.control.disconnectNode(this.clientIp);
            System.out.println("Connection loss with socket " + this.runner.getDestIP() + "!");
        }
    }
}
