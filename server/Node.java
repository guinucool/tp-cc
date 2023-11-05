/* Pacote de objetos do servidor (Tracker) */
package server;

import java.io.*;
import java.net.*;
import java.util.*;

import shared.*;

/**
 * @class Node
 * 
 * Representação de um node na visão do servidor (Tracker).
 * 
 * @param id Identificador gerado para o node.
 * @param ip IP do node.
 * @param udp Porta UDP associada ao node.
 * @param socket Socket usado para comunicar com o node.
 */
public class Node {
    
    private static List<String> iplist = new ArrayList<String>();

    private String ip;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Tracker tracker;

    public static class NodeException extends Exception {

        public NodeException(String msg) {
            super(msg);
        }
    }
    
    public Node(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = "";
        
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
    }

    public String getIp() {
        return this.ip;
    }

    public void registerNode(String arg) throws InvalidTrackArguments, NodeException {
        String[] ip = arg.split(".");

        if (ip.length != 4)
            throw new InvalidTrackArguments("Argument " + arg + " is invalid.");

        for (int i = 0; i < ip.length; i++) {
            try {
                int a = Integer.parseInt(ip[i]);

                if (a < 0 || a > 255)
                    throw new InvalidTrackArguments("Argument " + arg + "is not an ip.");

            } catch (NumberFormatException e) {
                throw new InvalidTrackArguments("Argument " + arg + "is not an ip.");
            }
        }

        if (iplist.contains(arg))
            throw new NodeException("Node " + arg + " already exists.");

        if (this.ip != "")
            throw new NodeException("Node is already registered with " + this.ip + ".");

        this.ip = arg;
        iplist.add(this.ip);
    }

    public void listenRequest() throws IOException {
        try {
            while (true) {
                TrackRequest req = (TrackRequest) this.input.readObject();

                switch (req.getCommand()) {
                    case REGS:
                        this.registerNode(req.getArgument());
                        this.output.writeObject(new TrackResponse(TrackResponse.Code.LURV));
                        break;

                    case QUIT:
                        this.output.writeObject(new TrackResponse(TrackResponse.Code.QUIT));
                        return;
                
                    default:
                        this.output.writeObject(new TrackResponse(TrackResponse.Code.FAIL));
                        return;
                }
            }
        } catch (ClassNotFoundException e) {
            this.output.writeObject(new TrackResponse(TrackResponse.Code.FAIL));
        } catch (InvalidTrackArguments e) {
            this.output.writeObject(new TrackResponse(TrackResponse.Code.INVA));
        } catch (NodeException e) {
            this.output.writeObject(new TrackResponse(TrackResponse.Code.INVN));
        }
    }

    /*public void closeNode() throws IOException {
        try {
            this.tracker.removeNode(this.ip);
        } catch (NodeException e) {
            this.closeSocket();
        }
    }*/
}
