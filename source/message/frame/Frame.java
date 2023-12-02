package message.frame;

import java.util.List;

import message.CommunicableException;
import message.Message;
import message.MessageException;

/**
 * Objeto que define uma mensagem do tipo frame, pronta a ser enviada por um frame runner.
 */
public class Frame extends Message {

    /* Construtor sem-argumento */
    public Frame(short flag) throws CommunicableException {
        super(flag);
    }

    /* Construtor sem-argumento resposta */
    public Frame(short identifier, short flag) throws CommunicableException {
        super(identifier, flag);
    }

    /* Construtor uni-argumento */
    public Frame(short flag, byte[] payload) throws CommunicableException {
        super(flag, payload);
    }

    /* Construtor uni-argumento resposta */
    public Frame(short identifier, short flag, byte[] payload) throws CommunicableException {
        super(identifier, flag, payload);
    }

    /* Construtor multi-argumento */
    public Frame(short flag, List<byte[]> payload) throws CommunicableException {
        super(flag, payload);
    }

    /* Construtor multi-argumento resposta */
    public Frame(short identifier, short flag, List<byte[]> payload) throws CommunicableException {
        super(identifier, flag, payload);
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

    /* Conversão de uma mensagem para String */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("(Frame)").append(super.toString());

        return builder.toString();
    }
}
