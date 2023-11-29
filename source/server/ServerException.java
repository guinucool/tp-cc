package server;

/**
 * Objeto que define uma exceção causada por um servidor.
 */
public class ServerException extends Exception {
    public ServerException(String msg) {
        super(msg);
    }
}
