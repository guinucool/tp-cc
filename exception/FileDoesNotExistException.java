package exception;

public class FileDoesNotExistException extends Exception {
    public FileDoesNotExistException(String msg) {
        super(msg);
    }
}
