package message;

/**
 * Interface que define, na generalidade, as propriedades de um objeto
 * que pode ser comunicado através de runners.
 */
public interface Communicable {

    /* Transforma o comunicável num formato comunicável para o runner usado */
    public Object toCommunicable() throws CommunicableException;
}
