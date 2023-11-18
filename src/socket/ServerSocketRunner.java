package socket;

import java.io.*;
import java.net.*;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no server side.
 */
public class ServerSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ServerSocketRunner(Socket socket) throws RunnerException {
        super(socket);
    }

    /* Setters */
    protected void setStream() throws IOException {
        super.setReader();
        super.setWriter();
    }
}
