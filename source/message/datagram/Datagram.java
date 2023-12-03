package message.datagram;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import message.CommunicableException;
import message.Message;
import message.MessageException;
import message.frame.Frame;
import model.Node;

public class Datagram extends Message {

    public final int HEADER_SIZE = Message.IDENTIFIER_SIZE + Short.BYTES * 2 + Integer.BYTES * 4;   /* Tamanho do header do datagrama */
    public static final int FLAG_SIZE = Short.BYTES;                                                /* Número de bytes da flag */
    public static final int QR_POSITION = (FLAG_SIZE * 8 - 1);                                      /* Posição bitwise da flag QR */
    public final int MAX_PACKET = 1500;                                                             /* Tamanho máximo do datagrama */
    public final int MAX_PAYLOAD = MAX_PACKET - HEADER_SIZE;                                        /* Tamanho máximo do payload */

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private short nrarguments;      /* Número de argumentos de uma mensagem */
    private byte[] payload;         /* Payload de uma mensagem */
    private boolean needsAck;       /* Verifica se a mensagem precisa de ack */
    private boolean isAck;          /* Verifica se a mensagem é um ack */
    private boolean isFragmented;   /* Verifica se a mensagem está fragmentada */
    private int totalSize;          /* Tamanho total do que se pretende enviar */
    private int fragmentOffset;     /* Offset do fragmento */
    private Node node;              /* Endereço e porta do datagrama */

    /* Construtor sem-argumento */
    public Datagram(short flag, Node node) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setDatagram(flag, node);
    }

    /* Construtor sem-argumento resposta */
    public Datagram(short identifier, short flag, Node node) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setDatagram(flag, node);
    }

    /* Construtor uni-argumento */
    public Datagram(short flag, byte[] payload, Node node) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor uni-argumento resposta */
    public Datagram(short identifier, short flag, byte[] payload, Node node) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor multi-argumento */
    public Datagram(short flag, List<byte[]> payload, Node node) throws CommunicableException {
        super();
        this.operation = Operation.QUERY;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor multi-argumento resposta */
    public Datagram(short identifier, short flag, List<byte[]> payload, Node node) throws CommunicableException {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor de cópia */
    public Datagram(Datagram datagram) {
        super(datagram);
        this.operation = datagram.operation;
        this.flag = datagram.flag;
        this.nrarguments = datagram.nrarguments;
        this.payload = datagram.payload.clone();
        this.needsAck = datagram.needsAck;
        this.isAck = datagram.isAck;
        this.isFragmented = datagram.isFragmented;
        this.totalSize = datagram.totalSize;
        this.fragmentOffset = datagram.fragmentOffset;
        this.node = (Node) datagram.node.clone();
    }

    /* Construtor livre */
    private Datagram(short identifier, Operation operation, short flag, short nrarguments, byte[] payload, boolean needsAck, boolean isAck, boolean isFragmented, int totalSize, int fragmentOffset, Node node) {
        super(identifier);
        this.operation = operation;
        this.flag = flag;
        this.nrarguments = nrarguments;
        this.payload = payload.clone();
        this.needsAck = needsAck;
        this.isAck = isAck;
        this.isFragmented = isFragmented;
        this.totalSize = totalSize;
        this.fragmentOffset = fragmentOffset;
        this.node = (Node) node.clone();
    }

    /**
     * Definição e verificação da flag
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setFlag(short flag) throws CommunicableException {

        /* Verifica se a flag é válida */
        if (flag < 0 || flag > 4095)
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
     * Definição e verificação do datagrama
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setDatagram(short flag, Node node) throws CommunicableException {
        this.setFlag(flag);
        this.nrarguments = 0;
        this.payload = new byte[0];
        this.needsAck = false;
        this.isAck = false;
        this.isFragmented = false;
        this.totalSize = 0;
        this.fragmentOffset = 0;
        this.node = (Node) node.clone();
    }

    /**
     * Definição e verificação do datagrama
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0).
     */
    private void setDatagram(short flag, byte[] payload, Node node) throws CommunicableException {
        this.setFlag(flag);
        this.nrarguments = 1;
        this.payload = payload.clone();
        this.needsAck = false;
        this.isAck = false;
        this.isFragmented = false;
        this.totalSize = payload.length;
        this.fragmentOffset = 0;
        this.node = (Node) node.clone();
    }

    /**
     * Definição e verificação do datagrama
     * 
     * @throws CommunicableException no caso da flag da mensagem fornecida
     * ser inválida (< 0) ou no caso de a lista de payload fornecida ser
     * inválida para a mensagem.
     */
    private void setDatagram(short flag, List<byte[]> payload, Node node) throws CommunicableException {
        this.setFlag(flag);
        this.setPayload(payload);
        this.needsAck = false;
        this.isAck = false;
        this.isFragmented = false;
        this.totalSize = this.payload.length;
        this.fragmentOffset = 0;
        this.node = (Node) node.clone();
    }

    /* Define a mensagem como ack */
    private void setAck() {
        this.isAck = true;
        this.destroyPayload();
    }

    /* Destroí o payload para fins de controlo e verificação */
    public void destroyPayload() {
        this.payload = new byte[0];
    }

    public int getChecksum() {

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

    /* Verifica se a mensagem precisa de um ack */
    public boolean needsAck() {
        return this.needsAck;
    }

    /* Verifica se a mensagem é um ack */
    public boolean isAck() {
        return this.isAck;
    }

    /* Verifica se a mensagem está fragmentada */
    public boolean isFragmented() {
        return this.isFragmented;
    }

    /* Devolve o offset de fragmentação da mensagem */
    public int getFragmentOffset() {
        return this.fragmentOffset;
    }

    /* Devolve o tamanho completo da mensagem */
    public int getTotalSize() {
        return this.totalSize;
    }

    /* Devolve o node associado à mensagem */
    public Node getNode() {
        return (Node) this.node.clone();
    }

    private byte[] getBytes() {

    }

    /* Cria um ack para uma mensagem, caso seja necessário */
    public Datagram ack() throws CommunicableException {

        /* Verifica se o datagrama é ackable */
        if (!this.needsAck())
            throw new CommunicableException("datagram-notackable");

        /* Cria o ack pretendido */
        Datagram ack = this.clone();
        ack.setAck();

        /* Devolve o ack */
        return ack;
    }

    /* Verifica se a mensagem fornecida é um ack da mensagem */
    public boolean isAck(Datagram ack) {

        /* Verifica se é um ack de um ackable */
        if (this.needsAck() && ack.isAck())
            return (this.getIdentifier() == ack.getIdentifier() && this.operation == ack.operation
                    && this.flag == ack.flag && this.nrarguments == ack.nrarguments
                    && this.needsAck == ack.needsAck && this.isFragmented == ack.isFragmented 
                    && this.totalSize == ack.totalSize && this.fragmentOffset == ack.fragmentOffset
                    && this.node.equals(ack.node));

        /* Verifica se é um ack de query */
        if (this.getOperation() == Operation.QUERY)
            return (this.getIdentifier() == ack.getIdentifier() && ack.operation == Operation.RESPONSE && this.node.equals(ack.node));

        /* Se não é nenhum dos casos */
        return false;
    }

    /* Fragmenta uma mensagem */
    public List<Datagram> fragment() throws MessageException {

        /* Verifica se há necessidade de fragmentar a mensagem */
        if (this.getPayloadSize() <= MAX_PAYLOAD)
            throw new MessageException("fragmentation-unnecessary");

        /* Calcula o payload para fragmentar */
        byte[] payload = this.getPayload();

        /* Cria a lista de fragmentos */
        List<Datagram> fragments = new ArrayList<>();

        /* Fragmenta o payload */
        for (int i = 0; i < payload.length; i = i + MAX_PAYLOAD) {
            
            int size = MAX_PAYLOAD;

            if (payload.length - i < MAX_PAYLOAD)
                size = payload.length - i;

            Datagram fragment = new Datagram(this.getIdentifier(), this.operation, this.flag, this.nrarguments, Arrays.copyOfRange(payload, i, size), true, false, true, payload.length, i, this.node);
            fragments.add(fragment);
        }

        /* Devolve a lista criada */
        return fragments;
    }

    public Datagram placeholder() {
        
        /* Cria o placeholder */
        Datagram placeholder = this.clone();
        placeholder.fragmentOffset = 0;
        placeholder.totalSize -= this.getPayloadSize();
    }

    public void join(Datagram fragment) {

    }

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {

        Datagram datagram = (Datagram) msg;
        return super.equals(msg) && this.operation == datagram.operation && this.flag == datagram.flag
                && this.nrarguments == datagram.nrarguments && this.needsAck == datagram.needsAck
                && this.isAck == datagram.isAck && this.isFragmented == datagram.isFragmented
                && this.totalSize == datagram.totalSize && this.fragmentOffset == datagram.fragmentOffset
                && this.node.equals(node);
    }

    /* Clona uma mensagem */
    public Datagram clone() {
        return new Datagram(this);
    }

    /* Cria uma representação em string de uma mensagem */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Datagram)").append(super.toString());
        builder.append("operation:").append(this.operation).append(";");
        builder.append("flag:").append(this.flag).append(";");
        builder.append("nrarguments:").append(this.nrarguments).append(";");
        builder.append("payload:");

        for (byte b : this.payload)
            builder.append(" ").append(b);

        builder.append(";");

        builder.append("needsAck:").append(this.needsAck).append(";");
        builder.append("isAck:").append(this.isAck).append(";");
        builder.append("isFragmented:").append(this.isFragmented).append(";");
        builder.append("totalSize:").append(this.totalSize).append(";");
        builder.append("fragmentOffset:").append(this.fragmentOffset).append(";");
        builder.append("node:").append(this.node.toString()).append(";");

        return builder.toString();
    }

    public byte[] toCommunicable() throws CommunicableException {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);

        short flag = this.flag;

        if (this.getOperation() == Operation.RESPONSE)
            flag |= 1 << QR_POSITION;
        
        try {
            stream.writeShort(this.identifier);
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

    public static int crc32checksum() {

    }
}
