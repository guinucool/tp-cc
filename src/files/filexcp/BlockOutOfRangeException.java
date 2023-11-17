package trackexcp;

public class BlockOutOfRangeException extends FileException {
   public BlockOutOfRangeException(String msg) {
	   super(msg);
   }
}