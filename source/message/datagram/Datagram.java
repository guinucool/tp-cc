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

    public final int HEADER_SIZE = Message.HEADER_SIZE + Integer.BYTES * 3;
    public final int MAX_PACKET = 1500;
    public final int MAX_PAYLOAD = MAX_PACKET - HEADER_SIZE;

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private short nrarguments;      /* Número de argumentos de uma mensagem */
    private byte[] payload;         /* Payload de uma mensagem */
    private boolean needsAck;
    private boolean isAck;
    private boolean isFragmented;
    private int totalSize;
    private int fragmentOffset;    
    private Node node;           /* Endereço e porta do datagrama */

    /* Construtor sem-argumento */
    public Datagram(short flag, Node node) throws CommunicableException {
        super(flag);
        this.node = (Node) node.clone();
    }

    /* Construtor sem-argumento resposta */
    public Datagram(short identifier, short flag, Node node) throws CommunicableException {
        super(identifier, flag);
        this.node = (Node) node.clone();
    }

    /* Construtor uni-argumento */
    public Datagram(short flag, byte[] payload, Node node) throws CommunicableException {
        super(flag, payload);
        this.node = (Node) node.clone();
    }

    /* Construtor uni-argumento resposta */
    public Datagram(short identifier, short flag, byte[] payload, Node node) throws CommunicableException {
        super(identifier, flag, payload);
        this.node = (Node) node.clone();
    }

    /* Construtor multi-argumento */
    public Datagram(short flag, List<byte[]> payload, Node node) throws CommunicableException {
        super(flag, payload);
        this.node = (Node) node.clone();
    }

    /* Construtor multi-argumento resposta */
    public Datagram(short identifier, short flag, List<byte[]> payload, Node node) throws CommunicableException {
        super(identifier, flag, payload);
        this.node = (Node) node.clone();
    }

    /* Construtor de cópia */
    public Datagram(Datagram datagram, boolean payload) {
        super(datagram, payload);
        this.node = (Node) datagram.node.clone();
    }

    /* Construtor binário */
    public Datagram(byte[] data) throws MessageException {
        super(data);
    }

    /* Construtor de fragmentos */
    private Datagram(short identifier, Operation operation, short flag, short nrarguments, byte[] payload, int totalSize, int fragmentOffset, Node node) {
        super(identifier, operation, flag, nrarguments, payload);
        this.totalSize = totalSize;
        this.fragmentOffset = fragmentOffset;
        this.node = (Node) node.clone();
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

    /* Cria um ack para uma mensagem, caso seja necessário */
    public Datagram ack() throws MessageException {

        /* Verifica se o datagrama é ackable */
        if (!this.needsAck())
            throw new MessageException("datagram-notackable");

        /* Cria o ack pretendido */
        Datagram ack = new Datagram(this, false);
        ack.isAck = true;

        return ack;
    }

    /* Verifica se a mensagem fornecida é um ack da mensagem */
    public boolean isAck(Datagram ack) {

        /* Verifica se é um ack de um ackable */
        if (this.needsAck())
            return (super.equals(ack) && ack.isAck && this.fragmentOffset == ack.fragmentOffset && this.totalSize == ack.totalSize && this.node.equals(ack.node));

        /* Verifica se é um ack de query */
        if (this.getOperation() == Operation.QUERY)
            return (this.getIdentifier() == ack.getIdentifier() && ack.getOperation() == Operation.RESPONSE && this.node.equals(ack.node));

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

            Datagram fragment = new Datagram(this.getIdentifier(), this.getOperation(), this.getFlag(), this.getNrArguments(), Arrays.copyOfRange(payload, i, size), this.getTotalSize(), i, this.getNode());

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

    public boolean equals(Datagram msg) {

    }

    public Datagram clone() {

    }

    public String toString() {

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
