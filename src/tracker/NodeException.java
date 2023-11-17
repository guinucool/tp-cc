package tracker;

public class NodeException extends Exception {

   public static class EmptyNodeIpException extends NodeException {
      public EmptyNodeIpException(String msg) {
         super(msg);
      }
   }

   public static class InvalidNodePortException extends NodeException {
      public InvalidNodePortException(String msg) {
         super(msg);
      }
   }

   public static class NodeRegisteredException extends NodeException {
      public NodeRegisteredException(String msg) {
         super(msg);
      }
   }

   public NodeException(String msg) {
	   super(msg);
   }
}