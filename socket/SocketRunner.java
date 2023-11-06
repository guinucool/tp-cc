package socket;

import java.io.*;
import java.net.*;

import exception.*;
import handler.*;
import shared.*;

public abstract class SocketRunner {
    
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ExceptionHandler handler;

    public SocketRunner(Socket socket) {
        this.socket = socket;
        this.handler = new NoHandler();
    }

    protected void setInputStream() throws IOException {
        this.input = new ObjectInputStream(this.socket.getInputStream());
    }

    protected void setOutputStream() throws IOException {
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
    }

    public abstract void setStream() throws IOException;

    protected void handle(String msg) {
        this.handler.handle(msg);
    }

    public TrackMessage readMessage() throws IOException, ClassNotFoundException {
        return (TrackMessage) this.input.readObject();
    }

    public void sendMessage(TrackMessage msg) throws IOException {
        this.output.writeObject(msg);
    }

    public abstract void translateMessage(TrackMessage msg) throws InvalidMessageTypeException;

    public boolean checkClosed() {
        return this.socket.isClosed();
    }

    protected void close() {
        try {
            if (!this.checkClosed()) this.socket.close();
        } catch (IOException e) {
            this.handle(e.getMessage());
        }
    }

    public String getSourceIP() {
        return this.socket.getLocalAddress().getHostAddress();
    }

    public String getDestIP() {
        return this.socket.getRemoteSocketAddress().toString().substring(1).split(":")[0];
    }
}
