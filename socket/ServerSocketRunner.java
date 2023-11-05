package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;

import exception.*;
import shared.*;

public class ServerSocketRunner extends SocketRunner implements Runnable {

    private ReentrantLock lock;
    // Tracker
    
    public ServerSocketRunner(Socket socket) {
        super(socket);
        this.lock = new ReentrantLock();
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

        if(req.getCommand() == TrackRequest.Command.QUIT)
            quitCommand();
            
    }

    private void quitCommand() {
        try {
            this.sendMessage(new TrackResponse(TrackResponse.Code.QUIT));
        } catch (IOException e) {
            this.handle(e.getMessage());
        } finally {
            this.close();
        }
    }

    public void shutdown() {
        this.lock.lock();
        this.close();
        this.lock.unlock();
    }
}
