package file.block;

/**
 * Objeto que define uma exceção que pode ser emitida por um
 * bloco.
 */
public class BlockException extends Exception {
    public BlockException(String msg) {
        super(msg);
    }
}
