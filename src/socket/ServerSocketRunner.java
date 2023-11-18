package socket;

import java.net.Socket;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no server side.
 */
public class ServerSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ServerSocketRunner(Socket socket) throws RunnerException {
        super(socket);
    }

    /* Setters */
    public void setStream() throws RunnerException {
        super.setReader();
        super.setWriter();
    }
}
