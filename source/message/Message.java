package message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.List;

/**
 * Objeto que define uma mensagem que, na generalidade, pode ser usada para comunicação
 * entre diferentes máquinas.
 */
public abstract class Message implements Communicable {

    public static final int IDENTIFIER_SIZE = Short.BYTES;      /* Número de bytes do identificador */

    private static short globalid = Short.MIN_VALUE;            /* Contador de identificador global */
    private short identifier;                                   /* Identificação da mensagem */

    /* Construtor vazio */
    public Message() {
        this.identifier = globalid++;
    }

    /* Construtor parametrizado */
    public Message(short identifier) {
        this.identifier = identifier;
    }

    public Message(Message message) {
        this.identifier = message.identifier;
    }

    /* Descobre qual é o identificador da mensagem */
    public short getIdentifier() {
        return this.identifier;
    }

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {

        if (this == msg)
            return true;

        if ((msg == null) || (this.getClass() != msg.getClass()))
            return false;

        return this.identifier == msg.identifier;
    }

    /* Cria uma representação em string de uma mensagem */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Message)");
        builder.append("identifier:").append(this.identifier).append(";");

        return builder.toString();
    }

    /**
     * Criação de um payload através de uma lista
     * 
     * @throws CommunicableException no caso de o sistema
     * ficar sem memória.
     */
    public static byte[] listToPayload(List<byte[]> payload) {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);

        /* Criação do payload */
        try {

            for (byte[] data : payload) {
                stream.writeInt(data.length);
                stream.write(data.clone());
            }

            stream.flush();
            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("payload-outofmemory");
        }
    }
}