package socket;

public class SocketRunnerException extends Exception {

   public static class ClosedRegisterSocketException extends SocketRunnerException {
      public ClosedRegisterSocketException(String msg) {
         super(msg);
      }
   }

   public SocketRunnerException(String msg) {
	   super(msg);
   }
}