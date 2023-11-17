package trackexcp;

public class NodeNotInTrackerException extends TrackerException {
   public NodeNotInTrackerException(String msg) {
	   super(msg);
   }
}