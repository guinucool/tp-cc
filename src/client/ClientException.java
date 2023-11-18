package client;

public class ClientException extends Exception {
    
    public static class ClientRejectedConnectionException extends ClientException {
        public ClientRejectedConnectionException(String msg) {
            super(msg);
        }
    }

    public static class FilenameExistsServerException extends ClientException {
        public FilenameExistsServerException(String msg) {
            super(msg);
        }
    }

    public static class ServerRejectedFileException extends ClientException {
        public ServerRejectedFileException(String msg) {
            super(msg);
        }
    }

    public ClientException(String msg) {
        super(msg);
    }
}

