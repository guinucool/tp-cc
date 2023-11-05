package client;

import java.io.*;
import java.net.*;

import shared.*;

public class Client {
    
    private String svip;
    private int svport;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public static class ClientException extends Exception {
    
        public ClientException(String msg) {
            super(msg);
        }
    }

    public Client(String ip) {
        this.svip = ip;
        this.svport = 9090;
    }

    public Client(String ip, int port) throws ClientException {
        setPort(port);
        this.svip = ip;
    }

    private void setPort(int port) throws ClientException {
        if (port < 0 || port > 65535)
            throw new ClientException("Server port " + port + " is invalid.");
        
        this.svport = port;
    }
    
    public void connect() throws IOException, UnknownHostException, IOException {
        this.socket = new Socket(svip, svport);

        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
    }

    public void sendRequest(TrackRequest req) throws IOException, ClassNotFoundException, InvalidTrackArguments, IOException, SocketCloseException {

        this.output.writeObject(req);
        TrackResponse res = (TrackResponse) this.input.readObject();

        switch (res.getCode()) {
            case LURV:
            System.out.println("Connected to the server wow!");
            break;
                
            default:
            this.close();
            break;
        }
    }

    public void close() throws SocketCloseException {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new SocketCloseException("Socket closed wrongly.");
        }
    }
}
