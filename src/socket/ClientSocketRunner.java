package socket;

import java.io.*;
import java.net.*;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no client side.
 */
public class ClientSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ClientSocketRunner(Socket socket) throws RunnerException {
        super(socket);
    }

    /* Setters */
    protected void setStream() throws IOException {
        super.setWriter();
        super.setReader();
    }
}
