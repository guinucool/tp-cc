package tools;

/**
 * Objeto que define um timeout para elementos de dados.
 */
public class Timeout<T> {

    private T object;           /* Objeto no qual se pretende implementar um timeout */
    private int timeout;        /* Timeout atual */

    /* Construtor parametrizado */
    public Timeout(T object) {
        this.object = object;
        this.timeout = 0;
    }

    /* Devolve o elemento do timeout */
    public T getObject() {
        return this.object;
    }

    /* Define o elemento do timeout */
    public void setObject(T object) {
        this.object = object;
    }

    /* Faz um tick no relógio do timeout */
    public void tick() {
        this.timeout++;
    }

    /* Faz um tick inverso no relógio do timeout */
    public void untick() {
        this.timeout--;
    }

    /* Reseta o timeout */
    public void reset() {
        this.timeout = 0;
    }

    /* Verifica se o elemento expirou */
    public boolean hasExpired(int maxTimeout) {
        return this.timeout >= maxTimeout;
    }

    /* Converte o par para formato string */
    public String toString() {
        
        StringBuilder builder = new StringBuilder();

        builder.append("(Timeout)");
        builder.append("object:").append(this.object.toString()).append(";");
        builder.append("timeout:").append(this.timeout).append(";");

        return builder.toString();
    }
}

