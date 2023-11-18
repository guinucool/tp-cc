package controller;

public class SocketControlException extends Exception {
 
    public static class RunnerCloseConnectionException extends SocketControlException {
        public RunnerCloseConnectionException(String msg) {
            super(msg);
        }
    }

    public static class RepeatedFilenameException extends SocketControlException {
        public RepeatedFilenameException(String msg) {
            super(msg);
        }
    }
    
    public static class FileRefusedException extends SocketControlException {
        public FileRefusedException(String msg) {
            super(msg);
        }
    }

    public SocketControlException(String msg) {
        super(msg);
    }
}
