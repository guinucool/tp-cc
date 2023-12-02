package model;

/**
 * Objeto que define uma exceção que pode ser emitida por clientes.
 */
public class ClientException extends Exception {
    public ClientException(String msg) {
        super(msg);
    }
}
