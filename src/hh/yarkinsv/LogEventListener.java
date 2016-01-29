package hh.yarkinsv;

import java.awt.event.ActionEvent;
import java.util.EventListener;

public interface LogEventListener extends EventListener {
    public void logEventAdded(String log);
}
