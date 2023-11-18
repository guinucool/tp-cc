package controller;

public class TrackerControlException extends Exception {
 
    public static class InvalidArgumentsException extends TrackerControlException {
        public InvalidArgumentsException(String msg) {
            super(msg);
        }
    }

    public TrackerControlException(String msg) {
        super(msg);
    }
}
