package client;

import java.io.IOException;
import java.net.*;

import shared.*;
import socket.*;

public class Client {
    
    private String server;
    private int port;
    private ClientSocketRunner runner;

    public Client(String sv) {
        this.server = sv;
        this.port = 9090;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws UnknownHostException, IOException {
        Socket socket = new Socket(this.server, this.port);
        this.runner = new ClientSocketRunner(socket);
        this.runner.setStream();
    }

    public void close() {
        TrackRequest req = new TrackRequest(TrackRequest.Command.QUIT);
        this.runner.run(req);
    }

    public void shutdown() {
        this.runner.shutdown();
    }

    public boolean checkClosed() {
        return this.runner.checkClosed();
    }
}
