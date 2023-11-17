package files;

public class BlockException extends Exception {

   public static class NodeExistsBlockException extends BlockException {
      public NodeExistsBlockException(String msg) {
         super(msg);
      }
   }

   public static class InvalidBlockOffsetException extends BlockException {
      public InvalidBlockOffsetException(String msg) {
         super(msg);
      }
   }

   public BlockException(String msg) {
	   super(msg);
   }
}