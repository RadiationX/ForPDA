package forpdateam.ru.forpda.model.data.remote.api.events;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent;
import forpdateam.ru.forpda.model.data.remote.IWebClient;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;

/**
 * Created by radiationx on 31.07.17.
 */

public class NotificationEventsApi {
    public final static Pattern inspectorFavoritesPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern inspectorQmsPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern webSocketEventPattern = Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]");

    private IWebClient webClient;

    public NotificationEventsApi(IWebClient webClient) {
        this.webClient = webClient;
    }

    public NotificationEvent parseWebSocketEvent(String message) {
        Matcher matcher = webSocketEventPattern.matcher(message);
        return parseWebSocketEvent(matcher);
    }

    public NotificationEvent parseWebSocketEvent(Matcher matcher) {
        NotificationEvent wsEvent = null;

        if (matcher.find()) {
            wsEvent = new NotificationEvent(
                    NotificationEvent.Type.NEW,
                    NotificationEvent.Source.THEME
            );
            //wsEvent.setUnknown1(Integer.parseInt(matcher.group(1)));
            //wsEvent.setUnknown2(Integer.parseInt(matcher.group(2)));

            switch (matcher.group(3)) {
                case NotificationEvent.SRC_TYPE_THEME:
                    wsEvent.setSource(NotificationEvent.Source.THEME);
                    break;
                case NotificationEvent.SRC_TYPE_SITE:
                    wsEvent.setSource(NotificationEvent.Source.SITE);
                    break;
                case NotificationEvent.SRC_TYPE_QMS:
                    wsEvent.setSource(NotificationEvent.Source.QMS);
                    break;
                default:
                    //// TODO: 02.10.17 сделать обратку нотификации форума
                    return null;
            }

            wsEvent.setSourceId(Integer.parseInt(matcher.group(4)));

            switch (Integer.parseInt(matcher.group(5))) {
                case NotificationEvent.SRC_EVENT_NEW:
                    wsEvent.setType(NotificationEvent.Type.NEW);
                    break;
                case NotificationEvent.SRC_EVENT_READ:
                    wsEvent.setType(NotificationEvent.Type.READ);
                    break;
                case NotificationEvent.SRC_EVENT_MENTION:
                    wsEvent.setType(NotificationEvent.Type.MENTION);
                    break;
                case NotificationEvent.SRC_EVENT_HAT_EDITED:
                    wsEvent.setType(NotificationEvent.Type.HAT_EDITED);
                    break;
            }
            wsEvent.setMessageId(Integer.parseInt(matcher.group(6)));
        }

        return wsEvent;
    }

    public List<NotificationEvent> getFavoritesEvents() throws Exception {
        NetworkResponse response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=fav");
        return getFavoritesEvents(response.getBody());
    }

    public List<NotificationEvent> getFavoritesEvents(String response) {
        List<NotificationEvent> events = new ArrayList<>();
        Matcher matcher = inspectorFavoritesPattern.matcher(response);
        while (matcher.find()) {
            //Log.e("events_lalala", "Matcher add event: " + matcher.group());
            NotificationEvent event = getFavoritesEvent(matcher);
            events.add(event);
        }
        return events;
    }

    public NotificationEvent getFavoritesEvent(Matcher matcher) {
        NotificationEvent event = new NotificationEvent(
                NotificationEvent.Type.NEW,
                NotificationEvent.Source.THEME
        );
        event.setSourceEventText(matcher.group());
        event.setSource(NotificationEvent.Source.THEME);
        event.setType(NotificationEvent.Type.NEW);
        event.setSourceId(Integer.parseInt(matcher.group(1)));
        event.setSourceTitle(ApiUtils.fromHtml(matcher.group(2)));
        event.setMsgCount(Integer.parseInt(matcher.group(3)));
        event.setUserId(Integer.parseInt(matcher.group(4)));
        event.setUserNick(ApiUtils.fromHtml(matcher.group(5)));
        event.setTimeStamp(Integer.parseInt(matcher.group(6)));
        event.setLastTimeStamp(Integer.parseInt(matcher.group(7)));
        event.setImportant(matcher.group(8).equals("1"));
        return event;
    }

    public List<NotificationEvent> getQmsEvents() throws Exception {
        NetworkResponse response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=qms");
        return getQmsEvents(response.getBody());
    }

    public List<NotificationEvent> getQmsEvents(String response) {
        List<NotificationEvent> events = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response);
        while (matcher.find()) {
            NotificationEvent event = getQmsEvent(matcher);
            events.add(event);
        }
        return events;
    }

    public NotificationEvent getQmsEvent(Matcher matcher) {
        NotificationEvent event = new NotificationEvent(
                NotificationEvent.Type.NEW,
                NotificationEvent.Source.QMS
        );
        event.setSourceEventText(matcher.group());
        event.setSource(NotificationEvent.Source.QMS);
        event.setType(NotificationEvent.Type.NEW);
        event.setSourceId(Integer.parseInt(matcher.group(1)));
        event.setSourceTitle(ApiUtils.fromHtml(matcher.group(2)));
        event.setUserId(Integer.parseInt(matcher.group(3)));
        event.setUserNick(ApiUtils.fromHtml(matcher.group(4)));
        event.setTimeStamp(Integer.parseInt(matcher.group(5)));
        event.setMsgCount(Integer.parseInt(matcher.group(6)));
        if (event.getUserNick().isEmpty() && event.getSourceId() == 0) {
            event.setUserNick("Сообщения 4PDA");
        }
        return event;
    }
}
