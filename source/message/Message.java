package message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto que define uma mensagem que, na generalidade, pode ser usada para comunicação
 * entre diferentes máquinas.
 */
public abstract class Message {

    /* Tipos de operação disponíveis */
    public enum Operation {
        QUERY,
        RESPONSE
    }

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private short nrarguments;      /* Número de argumentos de uma mensagem */
    private List<byte[]> payload;   /* Payload de uma mensagem */

    /* Construtor sem-argumento */
    public Message(Operation operation, short flag) throws MessageException {
        this.operation = operation;
        this.setFlag(flag);
        this.nrarguments = 0;
        this.payload = new ArrayList<>();
    }

    /* Construtor uni-argumento */
    public Message(Operation operation, short flag, byte[] payload) throws MessageException {
        this.operation = operation;
        this.setFlag(flag);
        this.nrarguments = 1;
        this.payload = new ArrayList<>();
        this.payload.add(payload.clone());
    }

    /* Construtor multi-argumento */
    public Message(Operation operation, short flag, List<byte[]> payload) throws MessageException {
        this.operation = operation;
        this.setFlag(flag);
        this.setPayload(payload);
    }

    /* Definição e verificação da flag */
    private void setFlag(short flag) throws MessageException {
        if (flag < 0)
            throw new MessageException("flag-invalid");

        this.flag = flag;
    }

    /* Definição e verificação de um payload multi-argumento */
    private void setPayload(List<byte[]> payload) throws MessageException {
        if (payload.size() > Short.MAX_VALUE)
            throw new MessageException("payload-big");
        
        this.nrarguments = (short) payload.size();
        this.payload = new ArrayList<>();

        for (byte[] data : payload)
            this.payload.add(data.clone());
    }

    /* Descobre qual o tipo de operação de uma mensagem */
    public Operation getOperation() {
        return this.operation;
    }

    /* Flag que identifica a mensagem */
    public short getFlag() {
        return this.flag;
    }

    /* Número de argumentos que uma mensagem carrega */
    public short getNrArguments() {
        return this.nrarguments;
    }

    /* Informação transportada pela mensagem */
    public List<byte[]> getPayload() {
        List<byte[]> data = new ArrayList<>();

        for (byte[] paydata : this.payload)
            data.add(paydata.clone());

        return data;
    }

    /* Decide quando duas mensagens são a mesma */
    public abstract boolean equals(Message msg);

    /* Clona uma mensagem */
    public abstract Message clone();

    /* Cria uma representação em string de uma mensagem */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Message)operation:").append(this.operation).append(";");
        builder.append("flag:").append(this.flag).append(";");
        builder.append("payload:").append(this.payload.size()).append(";");

        return builder.toString();
    }

    public static void main(String[] args) {

        byte[] buffer;

        ByteBuffer buf = ByteBuffer.wrap(buffer);
    }
}