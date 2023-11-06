package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;

import exception.*;
import server.*;
import shared.*;

public class ServerSocketRunner extends SocketRunner implements Runnable {

    private ReentrantLock lock;
    private Tracker tracker;
    
    public ServerSocketRunner(Socket socket, Tracker tracker) {
        super(socket);
        this.lock = new ReentrantLock();
        this.tracker = tracker;
    }

    public void setStream() throws IOException {
        this.setOutputStream();
        this.setInputStream();
    }

    public void run() {
        try {
            this.setStream();

            while (!this.checkClosed()) {
                TrackMessage msg = this.readMessage();
                this.lock.lock();
                this.translateMessage(msg);
                this.lock.unlock();
            }
        } catch (IOException | ClassNotFoundException e) {
            this.handle(e.getMessage());
            this.close();
        } catch (InvalidMessageTypeException e) {
            this.handle(e.getMessage());
            this.close();
            this.lock.unlock();
        }
    }

    public void translateMessage(TrackMessage msg) throws InvalidMessageTypeException {
        if (msg instanceof TrackResponse)
            throw new InvalidMessageTypeException("Received TrackResponse on server.");

        TrackRequest req = (TrackRequest) msg;

        if(req.getCommand() == TrackRequest.Command.REGS)
            this.registerNode(this.getDestIP());

        if(req.getCommand() == TrackRequest.Command.QUIT)
            this.quitCommand(this.getDestIP());
            
    }

    private void registerNode(String ip) {
        try {
            try {
                this.tracker.addNode(ip);
                System.out.println("Registered " + ip);
                this.sendMessage(new TrackResponse(TrackResponse.Code.LURV));
            } catch (NodeAlreadyExistsException e) {
                this.sendMessage(new TrackResponse(TrackResponse.Code.INVN));
                this.close();
            }
        } catch (IOException e) {
            this.handle(e.getMessage());
            this.close();
        }
    }

    private void quitCommand(String ip) {
        this.tracker.removeSingleNode(ip);

        try {
            this.sendMessage(new TrackResponse(TrackResponse.Code.QUIT));
        } catch (IOException e) {
            this.handle(e.getMessage());
        } finally {
            this.close();
            System.out.println("Disconnected " + ip);
        }
    }

    public void shutdown() {
        this.lock.lock();
        this.close();
        this.lock.unlock();
    }
}
