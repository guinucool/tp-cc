package model;

/**
 * Objeto que define uma exceção que pode ser emitida pelo tracker.
 */
public class TrackerException extends Exception {
    public TrackerException(String msg) {
        super(msg);
    }
}
