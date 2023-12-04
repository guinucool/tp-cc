package message;

/**
 * Objeto que define uma exceção que acontecer em objetos comunicáveis.
 */
public class CommunicableException extends Exception {
    public CommunicableException(String msg) {
        super(msg);
    }
}
