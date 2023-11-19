package socket;

import java.net.Socket;

/**
 * Objeto que define como devem ser inicializadas as streams do socket no client side.
 */
public class ClientSocketRunner extends SocketRunner {
    
    /* Constructors */
    public ClientSocketRunner(Socket socket) throws RunnerException {
        super(socket);
        this.setStream();
    }

    /* Setters */
    public void setStream() throws RunnerException {
        super.setWriter();
        super.setReader();
    }
}
