package tools;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Objeto que define a estrutura de um pedido que espera por uma
 * conclusão.
 */
public class Request {
    
    private boolean solved;         /* Caso o pedido seja resolvido com sucesso */
    private boolean failed;         /* Caso o pedido seja resolvido em falhanço */
    private Exception exception;    /* Exceção emitida pelo pedido */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();        /* Lock de leitura e escrita */
    private Lock read = this.rwlock.readLock();                         /* Lock de escrita */
    private Lock write = this.rwlock.writeLock();                       /* Lock de escrita e leitura */

    /* Construtor vazio */
    public Request() {
        this.solved = false;
        this.failed = false;
    }

    /* Resolve o pedido com sucesso */
    public void solve() {
        this.write.lock();
        this.solved = true;
        this.write.unlock();
    }

    /**
     * Resolve o pedido em falhanço
     * 
     * @param exception define a exceção que provocou o falhanço, sendo que pode
     * ser nula.
     */
    public void fail(Exception exception) {
        this.write.lock();
        this.failed = true;
        this.exception = exception;
        this.write.unlock();
    }

    /* Verifica se o pedido foi bem sucedido */
    public boolean isSolved() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Descobre se o pedido está bem sucedido */
        try {
            return this.solved;
        } finally {
            this.read.unlock();
        }
    }

    /* Verifica se o pedido falhou */
    public boolean isFailed() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Descobre se o pedido está falhado */
        try {
            return this.failed;
        } finally {
            this.read.unlock();
        }
    }

    /* Emite a exceção provocado pelo pedido */
    public void exceptionThrow() throws Exception {
        throw this.exception;
    }

    /* Transforma o pedido numa string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Request)");
        builder.append("solved:").append(this.solved).append(";");
        builder.append("failed:").append(this.failed).append(";");

        return builder.toString();
    }
}
