package message.datagram;

import message.Requestable;
import model.Node;
import tools.Request;

/**
 * Objeto que define a estrutura de um datagrama de pedido.
 */
public class DatagramRequest extends Datagram implements Requestable {
    
    private Request request;            /* Váriaveis de controlo da resolução do pedido */

    /* Construtor sem-argumento */
    public DatagramRequest(short flag, Node node) {
        super(flag, node);
        this.request = new Request();
    }

    /* Construtor uni-argumento */
    public DatagramRequest(short flag, byte[] payload, Node node) {
        super(flag, payload, node);
        this.request = new Request();
    }

    /* Transforma o pedido em formato de string */
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("(Request)").append(super.toString());
        builder.append("request:").append(this.request.toString()).append(";");

        return builder.toString();
    }

    /* Resolve o pedido com sucesso */
    public void solve() {
        this.request.solve();
    }

    /**
     * Resolve o pedido em falhanço
     * 
     * @param exception define a exceção que provocou o falhanço, sendo que pode
     * ser nula.
     */
    public void fail(Exception exception) {
        this.request.fail(exception);
    }

    /* Verifica se o pedido foi resolvido com sucesso */
    public boolean isSolved() {
        return this.request.isSolved();
    }

    /* Verifica se o pedido foi resolvido em falhanço */
    public boolean isFailed() {
        return this.request.isFailed();
    }

    /* Emite a exceção provocada pelo pedido */
    public void exceptionThrow() throws Exception {
        this.request.exceptionThrow();
    }
}
