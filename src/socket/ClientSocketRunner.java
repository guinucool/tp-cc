import java.io.*;
import java.net.*;

import sockexcp.*;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no client side.
 */
public class ClientSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ClientSocketRunner(Socket socket) throws SocketRunnerException, IOException {
        super(socket);
    }

    /* Setters */
    public void setStream() throws IOException {
        super.setWriter();
        super.setReader();
    }
}
