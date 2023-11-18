package view;

import socket.SocketRunner;

public class SocketView {
    
    private SocketRunner runner;

    /* Constructors */
    public SocketView(SocketRunner runner) {
        this.runner = runner;
    }

    /* View */
    public String getSourceIP() {
        return this.runner.getSourceIP();
    }

    public String getDestIP() {
        return this.runner.getDestIP();
    }

    public int getSourcePort() {
        return this.runner.getSourcePort();
    }

    public int getDestPort() {
        return this.runner.getDestPort();
    }

    public boolean isClosed() {
        return this.runner.isClosed();
    }
}
