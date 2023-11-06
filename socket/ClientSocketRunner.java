package socket;

import java.io.*;
import java.net.*;

import exception.*;
import shared.*;

public class ClientSocketRunner extends SocketRunner {

    public ClientSocketRunner(Socket socket) {
        super(socket);
    }
    
    public void setStream() throws IOException {
        this.setInputStream();
        this.setOutputStream();
    }

    public void run(TrackMessage msg) {
        try {
            this.sendMessage(msg);

            TrackMessage rep = this.readMessage();
            this.translateMessage(rep);
        } catch (IOException | ClassNotFoundException | InvalidMessageTypeException e) {
            this.handle(e.getMessage());
            this.close();
        }
    }

    public void translateMessage(TrackMessage msg) throws InvalidMessageTypeException {
        if (msg instanceof TrackRequest)
            throw new InvalidMessageTypeException("Received TrackRequest on client.");

        TrackResponse rep = (TrackResponse) msg;

        if (rep.getCode() == TrackResponse.Code.LURV)
            System.out.println("Register was sucessful!");

        if (rep.getCode() == TrackResponse.Code.QUIT || rep.getCode() == TrackResponse.Code.INVN)
            this.close();
    }

    public void shutdown() {
        this.close();
    }
}
