package view;

import message.*;
import model.tracker.Tracker;
import model.tracker.TrackerException;
import tracker.*;

public class TrackerView {

    private Tracker tracker;

    /* Constructors */
    public TrackerView(Tracker tracker) {
        this.tracker = tracker;
    }

    /* View */
    public TrackMessage getFile(String filename) throws TrackerException {
        return this.tracker.getFile(filename).toMessage();
    }
}