package tracker;

public class TrackerException extends Exception {

   public static class FilenameInTrackerException extends TrackerException {
      public FilenameInTrackerException(String msg) {
         super(msg);
      }
   }

   public static class FileNotInTrackerException extends TrackerException {
      public FileNotInTrackerException(String msg) {
         super(msg);
      }
   }

   public static class NodeNotInTrackerException extends TrackerException {
      public NodeNotInTrackerException(String msg) {
         super(msg);
      }
   }

   public TrackerException(String msg) {
	   super(msg);
   }
}