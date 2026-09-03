package file;

/**
 * Objeto que define uma exceção que pode ser emitida por representações
 * de ficheiro.
 */
public class FileException extends Exception {
    public FileException(String msg) {
        super(msg);
    }
}
