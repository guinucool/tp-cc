package view;

import tracker.*;
import tracker.TrackerException.FileNotInTrackerException;

public class TrackerView {

    private Tracker tracker;

    /* Constructors */
    public TrackerView(Tracker tracker) {
        this.tracker = tracker;
    }

    /* View */
    public String getFile(String filename) throws FileNotInTrackerException {
        return this.tracker.getFile(filename).toString();
    }

    public String getFiles() {
        return this.tracker.getFiles().toString();
    }
}