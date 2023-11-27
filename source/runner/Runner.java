package runner;

import message.Message;
import message.MessageException;

/**
 * Objeto que define uma estrutura geral para um runner de mensagens (frame ou datagram).
 */
public abstract class Runner {
    
    private static int globalid = 0;    /* Próximo identificador global de um runner  */

    private int id;     /* Identificador global de um runner */

    /* Construtor vazio */
    public Runner() {
        id = globalid++;
    }

    /* Identificador global do runner */
    public int getId() {
        return this.id;
    }

    /* Enviar uma mensagem através deste runner */
    public abstract void send(Message msg) throws RunnerException, MessageException;

    /* Receber uma mensagem através deste runner */
    public abstract Message receive() throws RunnerException, MessageException;

    /* Fecha o runner */
    public abstract void close();

    /* Verifica se o runner está fechado */
    public abstract boolean isClosed();

    /* Conversão do runner para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("(Runner)id:").append(this.id).append(";");

        return builder.toString();
    }
}
