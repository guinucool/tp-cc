package message.frame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import message.CommunicableException;
import message.Message;

/**
 * Objeto que define uma mensagem do tipo frame, pronta a ser enviada por um frame runner.
 */
public class Frame extends Message {

    public static final int HEADER_SIZE = IDENTIFIER_SIZE + Short.BYTES * 2 + Integer.BYTES;    /* Tamanho do header do frame */
    public static final int FLAG_SIZE = Short.BYTES;                                            /* Número de bytes da flag */
    public static final int QR_POSITION = (FLAG_SIZE * 8 - 1);                                  /* Posição bitwise da flag QR */

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private short nrarguments;      /* Número de argumentos de uma mensagem */
    private byte[] payload;         /* Payload de uma mensagem */

    /* Construtor sem-argumento */
    public Frame(short flag) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setFrame(flag);
    }

    /* Construtor sem-argumento resposta */
    public Frame(short identifier, short flag) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setFrame(flag);
    }

    /* Construtor uni-argumento */
    public Frame(short flag, byte[] payload) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setFrame(flag, payload);
    }

    /* Construtor uni-argumento resposta */
    public Frame(short identifier, short flag, byte[] payload) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setFrame(flag, payload);
    }

    /* Construtor multi-argumento */
    public Frame(short flag, List<byte[]> payload) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setFrame(flag, payload);
    }

    /* Construtor multi-argumento resposta */
    public Frame(short identifier, short flag, List<byte[]> payload) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setFrame(flag, payload);
    }

    /* Construtor de cópia */
    public Frame(Frame frame) {
        super(frame);
        this.operation = frame.operation;
        this.flag = frame.flag;
        this.nrarguments = frame.nrarguments;
        this.payload = frame.payload.clone();
    }

    /* Construtor livre */
    private Frame(short identifier, Operation operation, short flag, short nrarguments, byte[] payload) {
        super(identifier);
        this.operation = operation;
        this.flag = flag;
        this.nrarguments = nrarguments;
        this.payload = payload.clone();
    }

    /**
     * Definição e verificação da flag
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setFlag(short flag) throws CommunicableException {

        /* Verifica se a flag é válida */
        if (flag < 0)
            throw new CommunicableException("flag-invalid");

        this.flag = flag;
    }

    /**
     * Definição e verificação do payload através de uma lista
     * 
     * @throws CommunicableException no caso da lista de bytes fornecida ser
     * grande demais para a mensagem.
     */
    private void setPayload(List<byte[]> payload) throws CommunicableException {

        /* Verifica se o tamanho da lista é aceitável */
        if (payload.size() > Short.MAX_VALUE)
            throw new CommunicableException("payload-big");
        
        this.nrarguments = (short) payload.size();
        this.payload = Message.listToPayload(payload);
    }

    /**
     * Definição e verificação do frame
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setFrame(short flag) throws CommunicableException {
        this.setFlag(flag);
        this.nrarguments = 0;
        this.payload = new byte[0];
    }

    /**
     * Definição e verificação do frame
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setFrame(short flag, byte[] payload) throws CommunicableException {
        this.setFlag(flag);
        this.nrarguments = 1;
        this.payload = payload.clone();
    }

    /**
     * Definição e verificação do frame
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0) ou no caso de a lista de payload fornecida ser
     * inválida para a mensagem.
     */
    private void setFrame(short flag, List<byte[]> payload) throws CommunicableException {
        this.setFlag(flag);
        this.setPayload(payload);
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

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {

        Frame frame = (Frame) msg;
        return super.equals(msg) && this.operation == frame.operation && this.flag == frame.flag
                && this.nrarguments == frame.nrarguments && this.payload.equals(frame.payload);
    }

    /* Clona uma mensagem */
    public Frame clone() {
        return new Frame(this);
    }

    /* Cria uma representação em string de uma mensagem */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Frame)").append(super.toString());
        builder.append("operation:").append(this.operation).append(";");
        builder.append("flag:").append(this.flag).append(";");
        builder.append("nrarguments:").append(this.nrarguments).append(";");
        builder.append("payload:");

        for (byte b : this.payload)
            builder.append(" ").append(b);

        builder.append(";");

        return builder.toString();
    }

    /**
     * Transformação do frame em binário
     * 
     * @throws CommunicableException no caso de não existir memória suficiente para
     * alocar o frame em binário.
     */
    public byte[] toCommunicable() throws CommunicableException {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);

        short flag = this.flag;

        if (this.getOperation() == Operation.RESPONSE)
            flag |= 1 << QR_POSITION;
        
        try {
            stream.writeShort(this.getIdentifier());
            stream.writeShort(flag);
            stream.writeShort(this.nrarguments);
            stream.writeInt(this.payload.length);
            stream.write(this.getPayload());

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new CommunicableException("frame-outofmemory");
        }
    }

    /**
     * Transformação binária em frame
     * 
     * @throws CommunicableException no caso do objeto binário fornecido não ser um
     * frame válido.
     */
    public static Frame fromCommunicable(byte[] data) throws CommunicableException {

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para mensagem */
        try {
            Operation operation = Operation.QUERY;
            short identifier = stream.readShort();
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
            return new Frame(identifier, operation, flag, nrarguments, payload);

        } catch (IOException e) {
            throw new CommunicableException("message-invalid");
        }
    }
}
