package model;

/**
 * Objeto que define uma exceção que pode ser emitida pelo tracker.
 */
public class TrackerException extends Exception {

    public static class UsedFilenameException extends TrackerException {
        public UsedFilenameException(String msg) {
            super(msg);
        }
    }

    public TrackerException(String msg) {
        super(msg);
    }
}
