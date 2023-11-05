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

    public void translateMessage(TrackMessage msg) throws InvalidMessageTypeException {
        if (msg instanceof TrackRequest)
            throw new InvalidMessageTypeException("Received TrackRequest on client.");

        TrackResponse rep = (TrackResponse) msg;

        if (rep.getCode() == TrackResponse.Code.QUIT)
            this.close();
    }

    public void shutdown() {
        this.close();
    }
}
