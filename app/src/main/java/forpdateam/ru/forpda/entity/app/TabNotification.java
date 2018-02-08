package forpdateam.ru.forpda.entity.app;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.events.models.NotificationEvent;

/**
 * Created by radiationx on 26.09.17.
 */

public class TabNotification {
    private NotificationEvent.Source source;
    private NotificationEvent.Type type;
    private NotificationEvent event;
    private boolean webSocket = false;
    private List<NotificationEvent> loadedEvents = new ArrayList<>();
    private List<NotificationEvent> newEvents = new ArrayList<>();

    public NotificationEvent.Source getSource() {
        return source;
    }

    public void setSource(NotificationEvent.Source source) {
        this.source = source;
    }

    public NotificationEvent.Type getType() {
        return type;
    }

    public void setType(NotificationEvent.Type type) {
        this.type = type;
    }

    public NotificationEvent getEvent() {
        return event;
    }

    public void setEvent(NotificationEvent event) {
        this.event = event;
    }

    public List<NotificationEvent> getLoadedEvents() {
        return loadedEvents;
    }

    public List<NotificationEvent> getNewEvents() {
        return newEvents;
    }

    public boolean isWebSocket() {
        return webSocket;
    }

    public void setWebSocket(boolean webSocket) {
        this.webSocket = webSocket;
    }
}
