package worker;

/**
 * Objeto que define uma exceção criada por um worker.
 */
public class WorkerException extends Exception {
    public WorkerException(String msg) {
        super(msg);
    }
}
