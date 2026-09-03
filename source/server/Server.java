package server;

/**
 * Interface que define as regras de um servidor na generalidade.
 */
public interface Server {

    /* Endereço em que o servidor se encontra a correr no momento */
    public String getAddress();

    /* Porta que aloja o servidor */
    public int getPort();

    /* Audição por pedidos ou respostas ao servidor */
    public void listen() throws ServerException;

    /* Para o funcionamento do servidor */
    public void stop();
    
    /* Verifica se o servidor está fechado */
    public boolean isClosed();
}
