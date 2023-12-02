package tools;

/**
 * Objeto que define uma exceção que pode ser emitida por pedidos.
 */
public class RequestException extends Exception {
    public RequestException(String msg) {
        super(msg);
    }
}
