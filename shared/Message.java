/* Pacote de objetos partilhados entre o servidor e cliente */
package shared;

/**
 * @class Message
 * 
 * Estrutura básica de mensagem usada para comunicar de um lado para o outro.
 * 
 * @param type Tipo de mensagem a ser passado.
 * @param origin Origem da mensagem.
 * @param dest Destino da mensagem.
 */
public class Message {
    
    /**
     * @enum Type
     * 
     * Enumerador que define os vários tipos de mensagem existentes.
     */
    public enum Type {
        CONNECTION,
        CONFIRMATION
    }

    private Type type;
    private String origin;
    private String dest;

    public Message(Type type, String origin, String dest) {
        this.type = type;
        this.origin = origin;
        this.dest = dest;
    }

    public Type getType() {
        return this.type;
    }

    public String getOrigin() {
        return this.origin;
    }

    public String getDest() {
        return this.dest;
    }

    public boolean check(Type type, String origin, String dest) {
        return this.type == type && this.origin.equals(origin) && this.dest.equals(dest);
    }

    public boolean check(Type type, String dest) {
        return this.type == type && this.dest.equals(dest);
    }
}
