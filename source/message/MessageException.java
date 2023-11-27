package message;

/**
 * Objeto que define uma exceção de mensagem
 */
public class MessageException extends Exception {
    public MessageException(String msg) {
        super(msg);
    }
}
