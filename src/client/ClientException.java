package client;

public class ClientException extends Exception {

    public static class FilenameExistsException extends ClientException {
        public FilenameExistsException(String msg) {
            super(msg);
        }
    }

    public ClientException(String msg) {
        super(msg);
    }
}

