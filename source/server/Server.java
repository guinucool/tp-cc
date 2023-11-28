package server;

public interface Server {

    public void getAddress();

    public void getPort();

    public void listen();

    public void close();
    
    public boolean isClosed();
}
