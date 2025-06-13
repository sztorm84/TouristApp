package edp.touristapp.events;

import com.google.common.eventbus.EventBus;

public class AppEventBus {
    private static final EventBus eventBus = new EventBus();

    public static EventBus getInstance() {
        return eventBus;
    }

    private AppEventBus() {}
}