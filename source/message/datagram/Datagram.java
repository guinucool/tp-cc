package message.datagram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import message.CommunicableException;
import message.Message;
import model.Node;
import model.NodeException;

public class Datagram extends Message {

    public static final int HEADER_SIZE = Message.IDENTIFIER_SIZE + Short.BYTES * 2 + Integer.BYTES * 3;    /* Tamanho do header do datagrama */
    public static final int FLAG_SIZE = Short.BYTES;                                                        /* Número de bytes da flag */
    public static final int QR_POSITION = (FLAG_SIZE * 8 - 1);                                              /* Posição bitwise da flag QR */
    public static final int NA_POSITION = (FLAG_SIZE * 8 - 2);                                              /* Posição bitwise da flag NA */
    public static final int IA_POSITION = (FLAG_SIZE * 8 - 3);                                              /* Posição bitwise da flag IA */
    public static final int IF_POSITION = (FLAG_SIZE * 8 - 4);                                              /* Posição bitwise da flag IF */
    public static final int MAX_PACKET = 1500;                                                              /* Tamanho máximo do datagrama */
    public static final int MAX_PAYLOAD = MAX_PACKET - HEADER_SIZE;                                         /* Tamanho máximo do payload */

    private Operation operation;    /* Tipo de operação da mensagem */
    private short flag;             /* Flag de uma mensagem */
    private byte[] payload;         /* Payload de uma mensagem */
    private boolean needsAck;       /* Verifica se a mensagem precisa de ack */
    private boolean isAck;          /* Verifica se a mensagem é um ack */
    private boolean isFragmented;   /* Verifica se a mensagem está fragmentada */
    private int totalSize;          /* Tamanho total do que se pretende enviar */
    private int fragmentOffset;     /* Offset do fragmento */
    private Node node;              /* Endereço e porta do datagrama */

    /* Construtor sem-argumento */
    public Datagram(short flag, Node node) {
        super();
        this.operation = Operation.QUERY;
        this.setDatagram(flag, node);
    }

    /* Construtor sem-argumento resposta */
    public Datagram(short identifier, short flag, Node node) {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setDatagram(flag, node);
    }

    /* Construtor uni-argumento */
    public Datagram(short flag, byte[] payload, Node node) {
        super();
        this.operation = Operation.QUERY;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor uni-argumento resposta */
    public Datagram(short identifier, short flag, byte[] payload, Node node) {
        super(identifier);
        this.operation = Operation.RESPONSE;
        this.setDatagram(flag, payload, node);
    }

    /* Construtor de cópia */
    public Datagram(Datagram datagram) {
        super(datagram);
        this.operation = datagram.operation;
        this.flag = datagram.flag;
        this.payload = datagram.payload.clone();
        this.needsAck = datagram.needsAck;
        this.isAck = datagram.isAck;
        this.isFragmented = datagram.isFragmented;
        this.totalSize = datagram.totalSize;
        this.fragmentOffset = datagram.fragmentOffset;
        this.node = (Node) datagram.node.clone();
    }

    /* Construtor livre */
    private Datagram(short identifier, Operation operation, short flag, byte[] payload, boolean needsAck, boolean isAck, boolean isFragmented, int totalSize, int fragmentOffset, Node node) {
        super(identifier);
        this.operation = operation;
        this.flag = flag;
        this.payload = payload.clone();
        this.needsAck = needsAck;
        this.isAck = isAck;
        this.isFragmented = isFragmented;
        this.totalSize = totalSize;
        this.fragmentOffset = fragmentOffset;
        this.node = (Node) node.clone();
    }

    /* Definição e verificação da flag */
    private void setFlag(short flag) {

        /* Verifica se a flag é válida */
        if (flag < 0 || flag > 4095)
            throw new RuntimeException("flag-invalid");

        this.flag = flag;
    }

    /* Definição e verificação do datagrama */
    private void setDatagram(short flag, Node node) {
        this.setFlag(flag);
        this.payload = new byte[0];
        this.needsAck = false;
        this.isAck = false;
        this.isFragmented = false;
        this.totalSize = 0;
        this.fragmentOffset = 0;
        this.node = (Node) node.clone();
    }

    /* Definição e verificação do datagrama */
    private void setDatagram(short flag, byte[] payload, Node node) {

        /* Verifica se o payload é grande demais */
        if (this.operation == Operation.QUERY && payload.length > MAX_PAYLOAD)
            throw new RuntimeException("payload-big");

        /* Define as propriedades */
        this.setFlag(flag);
        this.payload = payload.clone();
        this.needsAck = false;
        this.isAck = false;
        this.isFragmented = false;
        this.totalSize = payload.length;
        this.fragmentOffset = 0;
        this.node = (Node) node.clone();
    }

    /* Define a mensagem como ack */
    private void setAck() {
        this.isAck = true;
        this.needsAck = false;
        this.destroyPayload();
    }

    /* Define a mensagem como placeholder */
    private void setPlaceholder() {

        /* Remove a necessidade de ack */
        this.needsAck = false;

        /* Armazena o atual payload */
        byte[] fragment = this.payload.clone();

        /* Cria um novo payload */
        this.payload = new byte[this.totalSize];
        this.totalSize -= fragment.length;
        
        /* Junta o fragmento já existente */
        this.joinPayload(fragment, fragmentOffset);
        this.fragmentOffset = 0;
    }

    /* Junta um fragmento ao payload */
    private void joinPayload(byte[] fragment, int offset) {
        for (int i = 0; i < fragment.length; i++)
            this.payload[offset + i] = fragment[i];
    }

    /* Destroí o payload para fins de controlo e verificação */
    public void destroyPayload() {
        this.payload = new byte[0];
    }

    /* Descobre qual a checksum para o pacote datagrama */
    public int getChecksum() {
        return crc32checksum(this.getBytes());
    }

    /* Descobre qual o tipo de operação de uma mensagem */
    public Operation getOperation() {
        return this.operation;
    }

    /* Flag que identifica a mensagem */
    public short getFlag() {
        return this.flag;
    }

    /* Número de argumentos da mensagem */
    public short getNrArguments() {
        return 1;
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

    /* Converte o datagrama em formato binário */
    private byte[] getBytes() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);

        /* Converte a flag e as flags necessárias para o datagrama */
        short flag = this.flag;

        if (this.operation == Operation.RESPONSE)
            flag |= 1 << QR_POSITION;

        if (this.needsAck)
            flag |= 1 << NA_POSITION;

        if (this.isAck)
            flag |= 1 << IA_POSITION;

        if (this.isFragmented)
            flag |= 1 << IF_POSITION;
        
        /* Converte o datagrama para binário */
        try {
            
            stream.writeShort(this.getIdentifier());
            stream.writeShort(flag);
            stream.writeInt(this.fragmentOffset);
            stream.writeInt(this.totalSize);
            stream.writeShort((short) this.payload.length);
            stream.write(this.getPayload());

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("datagram-outofmemory");
        }
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
                    && this.flag == ack.flag && this.isFragmented == ack.isFragmented 
                    && this.totalSize == ack.totalSize && this.fragmentOffset == ack.fragmentOffset
                    && this.node.equals(ack.node));

        /* Verifica se é um ack de query */
        if (this.getOperation() == Operation.QUERY)
            return (this.getIdentifier() == ack.getIdentifier() && ack.operation == Operation.RESPONSE && this.node.equals(ack.node));

        /* Se não é nenhum dos casos */
        return false;
    }

    /* Verifica se a mensagem fornecida é uma resposta da mensagem */
    public boolean isResponse(Datagram request) {

        /* Verifica se é um ack de query */
        if (this.getOperation() == Operation.RESPONSE)
            return (this.getIdentifier() == request.getIdentifier() && request.operation == Operation.QUERY && this.node.equals(request.node));

        /* Se não é nenhum dos casos */
        return false;
    }

    /* Fragmenta uma mensagem */
    public List<Datagram> fragment() {

        /* Verifica se há necessidade de fragmentar a mensagem */
        if (this.getPayloadSize() <= MAX_PAYLOAD)
            throw new RuntimeException("fragmentation-unnecessary");

        /* Calcula o payload para fragmentar */
        byte[] payload = this.getPayload();

        /* Cria a lista de fragmentos */
        List<Datagram> fragments = new ArrayList<>();

        /* Fragmenta o payload */
        for (int i = 0; i < payload.length; i = i + MAX_PAYLOAD) {
            
            int size = MAX_PAYLOAD;

            if (payload.length - i < MAX_PAYLOAD)
                size = payload.length - i;

            Datagram fragment = new Datagram(this.getIdentifier(), this.operation, this.flag, Arrays.copyOfRange(payload, i, i + size), true, false, true, payload.length, i, this.node);
            fragments.add(fragment);
        }

        /* Devolve a lista criada */
        return fragments;
    }

    public Datagram placeholder() throws CommunicableException {
        
        /* Verifica se o datagrama está fragementado */
        if (!this.isFragmented())
            throw new CommunicableException("datagram-unfragmented");

        /* Cria o placeholder */
        Datagram placeholder = this.clone();
        placeholder.setPlaceholder();

        return placeholder;
    }

    public void join(Datagram fragment) throws CommunicableException {

        /* Verifica se o datagrama está fragmentado */
        if (!this.isFragmented() || !fragment.isFragmented())
            throw new CommunicableException("datagram-unfragmented");

        /* Verifica se o fragmento faz parte deste todo */
        if (this.getIdentifier() != fragment.getIdentifier() || this.getOperation() != fragment.getOperation() || this.getFlag() != fragment.getFlag() || this.getNrArguments() != fragment.getNrArguments() || !this.getNode().equals(fragment.getNode()))
            throw new CommunicableException("datagram-different");

        /* Junta o fragmento */
        this.joinPayload(fragment.getPayload(), fragment.getFragmentOffset());
        this.totalSize -= fragment.getPayloadSize();

        /* Completa a fragmentação, caso seja necessário */
        if (this.totalSize == 0) {
            this.totalSize = this.getPayloadSize();
            this.isFragmented = false;
        }
    }

    /* Decide quando duas mensagens são a mesma */
    public boolean equals(Message msg) {

        Datagram datagram = (Datagram) msg;
        return super.equals(msg) && this.operation == datagram.operation && this.flag == datagram.flag
                && this.needsAck == datagram.needsAck && this.isAck == datagram.isAck && this.isFragmented == datagram.isFragmented
                && this.totalSize == datagram.totalSize && this.fragmentOffset == datagram.fragmentOffset
                && this.node.equals(datagram.node);
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

    /* Transformação do datagrama em binário */
    public DatagramPacket toCommunicable() {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        /* Converte o datagrama para binário */
        try {
            
            stream.writeInt(this.getChecksum());
            stream.write(this.getBytes());

            stream.flush();

            byte[] payload = barray.toByteArray();

            return new DatagramPacket(payload, payload.length, InetAddress.getByName(this.node.getAddress()), this.node.getPort());

        } catch (IOException e) {
            throw new RuntimeException("datagram-outofmemory");
        }
    }

    /**
     * Transformação binária em datagrama
     * 
     * @throws CommunicableException no caso do objeto binário fornecido não ser um
     * datagrama válido.
     * @throws NodeException no caso do pacote fornecido ter uma ligação inválida.
     */
    public static Datagram fromCommunicable(DatagramPacket packet) throws CommunicableException, NodeException {

        ByteArrayInputStream barray = new ByteArrayInputStream(packet.getData());
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para mensagem */
        try {

            int checksum = stream.readInt();

            Operation operation = Operation.QUERY;
            boolean needsAck = false;
            boolean isAck = false;
            boolean isFragmented = false;
            short identifier = stream.readShort();
            short flag = stream.readShort();
            int fragmentOffset = stream.readInt();
            int totalSize = stream.readInt();
            int size = stream.readShort();

            byte[] payload = new byte[size];
            stream.read(payload);

            /* Leitura da QR Flag */
            if ((flag >> QR_POSITION & 1) == 1)
                operation = Operation.RESPONSE;

            /* Leitura da NA Flag */
            if ((flag >> NA_POSITION & 1) == 1)
                needsAck = true;

            /* Leitura da IA Flag */
            if ((flag >> IA_POSITION & 1) == 1)
                isAck = true;

            /* Leitura da IF Flag */
            if ((flag >> IF_POSITION & 1) == 1)
                isFragmented = true;

            /* Conversão da flag para o seu formato */
            flag &= ~(1 << QR_POSITION);
            flag &= ~(1 << NA_POSITION);
            flag &= ~(1 << IA_POSITION);
            flag &= ~(1 << IF_POSITION);

            /* Criação do node */
            Node node = new Node(packet.getAddress().getHostAddress(), packet.getPort());

            /* Criação da mensagem */
            Datagram result = new Datagram(identifier, operation, flag, payload, needsAck, isAck, isFragmented, totalSize, fragmentOffset, node);

            if (result.getChecksum() != checksum)
                throw new CommunicableException("message-corrupted");

            return result;

        } catch (IOException e) {
            throw new CommunicableException("message-invalid");
        }
    }

    /* Cria uma checksum CRC-32 para a informação binária fornecida */
    public static int crc32checksum(byte[] data) {

        /* Cria um codificador CRC-32 */
        CRC32 crc = new CRC32();

        /* Cria uma checksum CRC-32 para o pacote */
        crc.update(data);
        return (int) crc.getValue();
    }
}
