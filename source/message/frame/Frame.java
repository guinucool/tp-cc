package message.frame;

import java.util.List;

import message.Message;
import message.MessageException;

/**
 * Objeto que define uma mensagem do tipo frame, pronta a ser enviada por um frame runner.
 */
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

    /* Conversão de uma mensagem para String */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("(Frame)").append(super.toString());

        return builder.toString();
    }

    public static void main(String[] args) throws MessageException {

        byte b1 = 10;
        byte b2 = 20;

        byte[] array = {b1, b2};

        Frame frame = new Frame(Operation.QUERY, (short) 0, array);

        System.out.println(frame.toString());

        for (byte b : frame.toByte()) {
            System.out.println(b);   
        }
    }
}
