package message.frame;

import java.util.List;

import message.Message;
import message.MessageException;

public class Frame extends Message {

    /* Construtor sem-argumento */
    public Frame(Operation operation, short flag) throws MessageException {
        super(operation, flag);
    }

    /* Construtor uni-argumento */
    public Frame(Operation operation, short flag, byte[] payload) throws MessageException {
        super(operation, flag, payload);
    }

    /* Construtor multi-argumento */
    public Frame(Operation operation, short flag, List<byte[]> payload) throws MessageException {
        super(operation, flag, payload);
    }

    /* Construtor de cópia */
    public Frame(Frame frame) {
        super(frame);
    }

    /* Construtor binário */
    public Frame(byte[] data) throws MessageException {
        super(data);
    }

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {
        return super.equals(msg) && this.getPayload().equals(msg.getPayload());
    }

    /* Clona uma mensagem */
    public Message clone() {
        return new Frame(this);
    }
}
