package files;

public class PathException extends Exception {

    public static class PathNotFoundException extends PathException {
        public PathNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class FileNotInPathException extends PathException {
        public FileNotInPathException(String msg) {
            super(msg);
        }
    }

    public PathException(String msg) {
        super(msg);
    }
}
