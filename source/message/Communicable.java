package message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface que define, na generalidade, as propriedades de um objeto
 * que pode ser comunicado através de runners.
 */
public interface Communicable {

    /* Tipos de operação disponíveis */
    public enum Operation {
        QUERY,
        RESPONSE
    }

    /* Devolve o identificador uníco do comunicável */
    public short getIdentifier();

    /* Descobre qual o tipo de operação de uma mensagem */
    public Operation getOperation();

    /* Flag que identifica a mensagem */
    public short getFlag();

    /* Número de argumentos que uma mensagem carrega */
    public short getNrArguments();

    /* Payload completo de uma mensagem */
    public byte[] getPayload();

    /**
     * Informação transportada pela mensagem
     * 
     * @throws MessageException no caso do payload não corresponder a uma lista binária
     * válida.
     */
    public default List<byte[]> getPayloadList() throws CommunicableException {

        List<byte[]> payload = new ArrayList<>();

        if (this.getNrArguments() == 1) {
            payload.add(this.getPayload());
            return payload;
        }

        /* Descodificação para lista em caso de multi-argumento */
        ByteArrayInputStream barray = new ByteArrayInputStream(this.getPayload());
        DataInputStream stream = new DataInputStream(barray);

        for (int i = 0; i < this.getNrArguments(); i++) {
            try {
                int size = stream.readInt();

                byte[] buf = new byte[size];
                stream.read(buf);
                payload.add(buf);
            } catch (IOException e) {
                throw new CommunicableException("payload-invalid");
            }
        }

        return payload;
    }

    /* Tamanho do payload de uma mensagem */
    public default int getPayloadSize() {
        return this.getPayload().length;
    }

    /* Transforma o comunicável num formato comunicável para o runner usado */
    public Object toCommunicable();
}
