package exception;

public class InvalidServerPort extends Exception {
    public InvalidServerPort(String msg) {
        super(msg);
    }
}