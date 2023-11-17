package files;

public class FileException extends Exception {

   public static class EmptyObjectHashException extends FileException {
      public EmptyObjectHashException(String msg) {
         super(msg);
      }
   }

   public static class EmptyFilenameException extends FileException {
      public EmptyFilenameException(String msg) {
         super(msg);
      }
   }

   public static class InvalidBlocksException extends FileException {
      public InvalidBlocksException(String msg) {
         super(msg);
      }
   }

   public static class InvalidBlocksizeException extends FileException {
      public InvalidBlocksizeException(String msg) {
         super(msg);
      }
   }

   public static class InvalidFileSizeException extends FileException {
      public InvalidFileSizeException(String msg) {
         super(msg);
      }
   }

   public static class BlockOutOfRangeException extends FileException {
      public BlockOutOfRangeException(String msg) {
         super(msg);
      }
   }

   public FileException(String msg) {
	   super(msg);
   }
}