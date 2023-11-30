package message;

/**
 * Objeto que define uma exceção que acontecer em objetos comunicáveis.
 */
public class CommunicableException extends MessageException {
    public CommunicableException(String msg) {
        super(msg);
    }
}
