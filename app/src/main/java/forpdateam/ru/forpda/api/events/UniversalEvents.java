package forpdateam.ru.forpda.api.events;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.events.models.UniversalEvent;

/**
 * Created by radiationx on 31.07.17.
 */

public class UniversalEvents {
    public final static Pattern inspectorFavoritesPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern inspectorQmsPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern webSocketEventPattern = Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]");

    public UniversalEvent parseWebSocketEvent(String message) {
        Matcher matcher = webSocketEventPattern.matcher(message);
        return parseWebSocketEvent(matcher);
    }

    public UniversalEvent parseWebSocketEvent(Matcher matcher) {
        UniversalEvent wsEvent = null;

        if (matcher.find()) {
            wsEvent = new UniversalEvent();
            //wsEvent.setUnknown1(Integer.parseInt(matcher.group(1)));
            //wsEvent.setUnknown2(Integer.parseInt(matcher.group(2)));

            switch (matcher.group(3)) {
                case UniversalEvent.SRC_TYPE_THEME:
                    wsEvent.setSource(UniversalEvent.Source.THEME);
                    break;
                case UniversalEvent.SRC_TYPE_SITE:
                    wsEvent.setSource(UniversalEvent.Source.SITE);
                    break;
                case UniversalEvent.SRC_TYPE_QMS:
                    wsEvent.setSource(UniversalEvent.Source.QMS);
                    break;
            }

            wsEvent.setSourceId(Integer.parseInt(matcher.group(4)));

            switch (Integer.parseInt(matcher.group(5))) {
                case UniversalEvent.SRC_EVENT_NEW:
                    wsEvent.setEvent(UniversalEvent.Event.NEW);
                    break;
                case UniversalEvent.SRC_EVENT_READ:
                    wsEvent.setEvent(UniversalEvent.Event.READ);
                    break;
                case UniversalEvent.SRC_EVENT_MENTION:
                    wsEvent.setEvent(UniversalEvent.Event.MENTION);
                    break;
                case UniversalEvent.SRC_EVENT_HAT_EDITED:
                    wsEvent.setEvent(UniversalEvent.Event.HAT_EDITED);
                    break;
            }
            wsEvent.setMessageId(Integer.parseInt(matcher.group(6)));
        }

        return wsEvent;
    }

    public List<UniversalEvent> getFavoritesEvents() throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=fav");
        return getFavoritesEvents(response.getBody());
    }

    public List<UniversalEvent> getFavoritesEvents(String response) {
        List<UniversalEvent> events = new ArrayList<>();
        Matcher matcher = inspectorFavoritesPattern.matcher(response);
        while (matcher.find()) {
            UniversalEvent event = getFavoritesEvent(matcher);
            events.add(event);
        }
        return events;
    }

    public UniversalEvent getFavoritesEvent(Matcher matcher) {
        UniversalEvent event = new UniversalEvent();
        event.setSourceEventText(matcher.group());
        event.setSource(UniversalEvent.Source.THEME);
        event.setEvent(UniversalEvent.Event.NEW);
        event.setSourceId(Integer.parseInt(matcher.group(1)));
        event.setSourceTitle(Utils.fromHtml(matcher.group(2)));
        event.setMsgCount(Integer.parseInt(matcher.group(3)));
        event.setUserId(Integer.parseInt(matcher.group(4)));
        event.setUserNick(Utils.fromHtml(matcher.group(5)));
        event.setTimeStamp(Integer.parseInt(matcher.group(6)));
        event.setLastTimeStamp(Integer.parseInt(matcher.group(7)));
        event.setImportant(matcher.group(8).equals("1"));
        return event;
    }

    public List<UniversalEvent> getQmsEvents() throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=qms");
        return getQmsEvents(response.getBody());
    }

    public List<UniversalEvent> getQmsEvents(String response) {
        List<UniversalEvent> events = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response);
        while (matcher.find()) {
            UniversalEvent event = getQmsEvent(matcher);
            events.add(event);
        }
        return events;
    }

    public UniversalEvent getQmsEvent(Matcher matcher) {
        UniversalEvent event = new UniversalEvent();
        event.setSourceEventText(matcher.group());
        event.setSource(UniversalEvent.Source.QMS);
        event.setEvent(UniversalEvent.Event.NEW);
        event.setSourceId(Integer.parseInt(matcher.group(1)));
        event.setSourceTitle(Utils.fromHtml(matcher.group(2)));
        event.setUserId(Integer.parseInt(matcher.group(3)));
        event.setUserNick(Utils.fromHtml(matcher.group(4)));
        event.setTimeStamp(Integer.parseInt(matcher.group(5)));
        event.setMsgCount(Integer.parseInt(matcher.group(6)));
        if (event.getUserNick().isEmpty() && event.getSourceId() == 0) {
            event.setUserNick("Сообщения 4PDA");
        }
        return event;
    }
}
