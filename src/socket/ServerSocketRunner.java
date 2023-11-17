import java.io.*;
import java.net.*;

import sockexcp.*;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no server side.
 */
public class ServerSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ServerSocketRunner(Socket socket) throws SocketRunnerException, IOException {
        super(socket);
    }

    /* Setters */
    public void setStream() throws IOException {
        super.setReader();
        super.setWriter();
    }
}
