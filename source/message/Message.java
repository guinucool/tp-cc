package message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto que define uma mensagem que, na generalidade, pode ser usada para comunicação
 * entre diferentes máquinas.
 */
public abstract class Message implements Communicable {

    /* Tipos de operação disponíveis */
    public enum Operation {
        QUERY,
        RESPONSE
    }

    public static final int HEADER_SIZE = Short.BYTES * 2;      /* Número de bytes do header */
    public static final int FLAG_SIZE = Short.BYTES;            /* Número de bytes da flag */
    public static final int QR_POSITION = (FLAG_SIZE * 8 - 1);  /* Posição bitwise da flag QR */

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private short nrarguments;      /* Número de argumentos de uma mensagem */
    private byte[] payload;         /* Payload de uma mensagem */

    /* Construtor sem-argumento */
    public Message(Operation operation, short flag) throws CommunicableException {
        this.operation = operation;
        this.setFlag(flag);
        this.nrarguments = 0;
        this.payload = new byte[0];
    }

    /* Construtor uni-argumento */
    public Message(Operation operation, short flag, byte[] payload) throws CommunicableException {
        this.operation = operation;
        this.setFlag(flag);
        this.nrarguments = 1;
        this.payload = payload.clone();
    }

    /* Construtor multi-argumento */
    public Message(Operation operation, short flag, List<byte[]> payload) throws CommunicableException {
        this.operation = operation;
        this.setFlag(flag);
        this.setPayload(payload);
    }

    /* Construtor de cópia */
    public Message(Message msg) {
        this.operation = msg.operation;
        this.flag = msg.flag;
        this.nrarguments = msg.nrarguments;
        this.payload = msg.payload.clone();
    }

    /** 
     * Construtor binário
     * 
     * @throws MessageException no caso de a mensagem binária fornecida
     * ter o formato errado.
     */
    public Message(byte[] data) throws MessageException {

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para mensagem */
        try {
            Operation operation = Operation.QUERY;
            short flag = stream.readShort();
            short nrarguments = stream.readShort();
            int size = stream.readInt();

            byte[] payload = new byte[size];
            stream.read(payload);

            /* Leitura da QR Flag */
            if ((flag >> QR_POSITION & 1) == 1)
                operation = Operation.RESPONSE;

            /* Conversão da flag para o seu formato */
            flag &= ~(1 << QR_POSITION);

            /* Criação da mensagem */
            this.operation = operation;
            this.flag = flag;
            this.nrarguments = nrarguments;
            this.payload = payload;

        } catch (IOException e) {
            throw new MessageException("message-invalid");
        }
    }

    /**
     * Definição e verificação da flag
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setFlag(short flag) throws CommunicableException {
        if (flag < 0)
            throw new CommunicableException("flag-invalid");

        this.flag = flag;
    }

    /**
     * Definição e verificação de um payload multi-argumento
     * 
     * @throws CommunicableException no caso de a lista de payload fornecida
     * ser demasiado grande para o sistema ou no caso de o sistema
     * ficar sem memória.
     */
    private void setPayload(List<byte[]> payload) throws CommunicableException {
        if (payload.size() > Short.MAX_VALUE)
            throw new CommunicableException("payload-big");
        
        this.nrarguments = (short) payload.size();

        /* Criação do payload */
        try {
            ByteArrayOutputStream barray = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(barray);

            for (byte[] data : payload) {
                stream.writeInt(data.length);
                stream.write(data.clone());
            }

            stream.flush();
            this.payload = barray.toByteArray();
        } catch (IOException e) {
            throw new CommunicableException("payload-outofmemory");
        }
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

    /* Payload completo de uma mensagem */
    public byte[] getPayload() {
        return this.payload.clone();
    }

    /**
     * Informação transportada pela mensagem
     * 
     * @throws MessageException no caso do payload não corresponder a uma lista binária
     * válida.
     */
    public List<byte[]> getPayloadList() throws MessageException {

        List<byte[]> payload = new ArrayList<>();

        if (this.nrarguments == 1) {
            payload.add(this.payload.clone());
            return payload;
        }

        /* Descodificação para lista em caso de multi-argumento */
        ByteArrayInputStream barray = new ByteArrayInputStream(this.payload.clone());
        DataInputStream stream = new DataInputStream(barray);

        for (int i = 0; i < this.nrarguments; i++) {
            try {
                int size = stream.readInt();

                byte[] buf = new byte[size];
                stream.read(buf);
                payload.add(buf);
            } catch (IOException e) {
                throw new MessageException("payload-invalid");
            }
        }

        return payload;
    }

    /* Tamanho do payload de uma mensagem */
    public int getPayloadSize() {
        return this.payload.length;
    }

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {

        if (this == msg)
            return true;

        if ((msg == null) || (this.getClass() != msg.getClass()))
            return false;

        return (this.operation == msg.operation && this.flag == msg.flag && this.nrarguments == msg.nrarguments);
    }

    /* Clona uma mensagem */
    public abstract Message clone();

    /* Cria uma representação em string de uma mensagem */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Message)operation:").append(this.operation).append(";");
        builder.append("flag:").append(this.flag).append(";");
        builder.append("nrarguments:").append(this.nrarguments).append(";");
        builder.append("payload:");

        for (byte b : this.payload) {
            builder.append(" ").append(b);   
        }

        builder.append(";");

        return builder.toString();
    }

    /**
     * Transformação do frame em binário
     * 
     * @throws MessageException no caso de não existir memória suficiente para
     * alocar o frame em binário.
     */
    public byte[] toCommunicable() throws CommunicableException {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);

        short flag = this.flag;

        if (this.getOperation() == Operation.RESPONSE)
            flag |= 1 << QR_POSITION;
        
        try {
            stream.writeShort(flag);
            stream.writeShort(this.nrarguments);
            stream.writeInt(this.getPayloadSize());
            stream.write(this.getPayload());

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new CommunicableException("frame-outofmemory");
        }
    }
}