package runner;

import message.Communicable;
import message.Message;

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

    /* Endereço de alojamento local */
    public abstract String getAddress();

    /* Porta de alojamento local */
    public abstract int getPort();

    /**
     * Enviar uma mensagem através deste runner
     * 
     * @throws RunnerException no caso de a conexão do socket ter sido desligada
     * por instabilidades.
     */
    public abstract void send(Communicable msg) throws RunnerException;

    /**
     * Receber uma mensagem através deste runner
     * 
     * @throws RunnerException no caso de a conexão do socket ter sido desligada
     * por instabilidades.
     */
    public abstract Message receive() throws RunnerException;

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
