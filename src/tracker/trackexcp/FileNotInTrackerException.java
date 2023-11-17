package trackexcp;

public class FileNotInTrackerException extends TrackerException {
   public FileNotInTrackerException(String msg) {
	   super(msg);
   }
}