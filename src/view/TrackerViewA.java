package view;

import message.TrackMessage;
import message.TrackMessage.Type;

import model.tracker.Tracker;
import model.tracker.TrackerException;

public class TrackerViewA {

    private Tracker tracker;

    /* Constructors */
    public TrackerView() {
        this.tracker = Tracker.getInstance();
    }

    /* View */
    public TrackMessage getFile(String filename) throws TrackerException {
        return new TrackMessage(Type.RESPONSE, 200, this.tracker.getFile(filename).toStrings());
    }
}