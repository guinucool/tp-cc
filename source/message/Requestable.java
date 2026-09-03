package message;

/**
 * Interface que define quais as regras de um pedido de uma mensagem no geral.
 */
public interface Requestable extends Communicable {

    /* Resolve um pedido para caso de sucesso */
    public void solve();

    /* Resolve um pedido para caso de falhanço */
    public void fail(Exception exception);

    /* Verifica se um pedido foi bem sucedido */
    public boolean isSolved();

    /* Verifica se um pedido foi mal sucedido */
    public boolean isFailed();

    /* Verifica se um pedido foi resolvido */
    public default boolean isResolved() {
        return (this.isSolved() || this.isFailed());
    }

    /* Emite a exceção provocada pelo pedido */
    public void exceptionThrow() throws Exception;
}
