package hh.yarkinsv;

import java.util.EventListener;

public interface LogEventListener extends EventListener {
    void logEventAdded(String log);
}
