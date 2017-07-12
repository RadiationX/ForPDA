package forpdateam.ru.forpda.api.events;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.events.models.WebSocketEvent;

/**
 * Created by radiationx on 10.07.17.
 */

public class Events {
    public final static Pattern inspectorFavoritesPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern inspectorQmsPattern = Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)");
    public final static Pattern webSocketEventPattern = Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]");

    public WebSocketEvent parseWebSocketEvent(String message) {
        Matcher matcher = webSocketEventPattern.matcher(message);
        return parseWebSocketEvent(matcher);
    }

    public WebSocketEvent parseWebSocketEvent(Matcher matcher) {
        WebSocketEvent webSocketEvent = null;

        if (matcher.find()) {
            webSocketEvent = new WebSocketEvent();
            webSocketEvent.setUnknown1(Integer.parseInt(matcher.group(1)));
            webSocketEvent.setUnknown2(Integer.parseInt(matcher.group(2)));

            switch (matcher.group(3)) {
                case WebSocketEvent.SRC_TYPE_THEME:
                    webSocketEvent.setType(WebSocketEvent.TYPE_THEME);
                    break;
                case WebSocketEvent.SRC_TYPE_SITE:
                    webSocketEvent.setType(WebSocketEvent.TYPE_SITE);
                    break;
                case WebSocketEvent.SRC_TYPE_QMS:
                    webSocketEvent.setType(WebSocketEvent.TYPE_QMS);
                    break;
            }

            webSocketEvent.setId(Integer.parseInt(matcher.group(4)));

            switch (Integer.parseInt(matcher.group(5))) {
                case WebSocketEvent.SRC_EVENT_NEW:
                    webSocketEvent.setEventCode(WebSocketEvent.EVENT_NEW);
                    break;
                case WebSocketEvent.SRC_EVENT_READ:
                    webSocketEvent.setEventCode(WebSocketEvent.EVENT_READ);
                    break;
                case WebSocketEvent.SRC_EVENT_MENTION:
                    webSocketEvent.setEventCode(WebSocketEvent.EVENT_MENTION);
                    break;
                case WebSocketEvent.SRC_EVENT_HAT_CHANGE:
                    webSocketEvent.setEventCode(WebSocketEvent.EVENT_HAT_CHANGE);
                    break;
            }

            webSocketEvent.setMessageId(Integer.parseInt(matcher.group(6)));
        }

        return webSocketEvent;
    }

    public List<NotificationEvent> getFavoritesEvents() throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=fav");
        return getFavoritesEvents(response.getBody());
    }

    public List<NotificationEvent> getFavoritesEvents(String response) {
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorFavoritesPattern.matcher(response);
        while (matcher.find()) {
            NotificationEvent notificationEvent = getFavoritesEvent(matcher);
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    public NotificationEvent getFavoritesEvent(Matcher matcher) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setSource(matcher.group());
        notificationEvent.setThemeId(Integer.parseInt(matcher.group(1)));
        notificationEvent.setThemeTitle(Utils.fromHtml(matcher.group(2)));
        notificationEvent.setMessageCount(Integer.parseInt(matcher.group(3)));
        notificationEvent.setUserId(Integer.parseInt(matcher.group(4)));
        notificationEvent.setUserNick(Utils.fromHtml(matcher.group(5)));
        notificationEvent.setTimeStamp(Integer.parseInt(matcher.group(6)));
        notificationEvent.setReadTimeStamp(Integer.parseInt(matcher.group(7)));
        notificationEvent.setImportant(matcher.group(8).equals("1"));
        return notificationEvent;
    }

    public List<NotificationEvent> getQmsEvents() throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=inspector&CODE=qms");
        return getQmsEvents(response.getBody());
    }

    public List<NotificationEvent> getQmsEvents(String response) {
        List<NotificationEvent> qmsThemes = new ArrayList<>();
        Matcher matcher = inspectorQmsPattern.matcher(response);
        while (matcher.find()) {
            NotificationEvent notificationEvent = getQmsEvent(matcher);
            qmsThemes.add(notificationEvent);
        }
        return qmsThemes;
    }

    public NotificationEvent getQmsEvent(Matcher matcher) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setSource(matcher.group());
        notificationEvent.setThemeId(Integer.parseInt(matcher.group(1)));
        notificationEvent.setThemeTitle(Utils.fromHtml(matcher.group(2)));
        notificationEvent.setUserId(Integer.parseInt(matcher.group(3)));
        notificationEvent.setUserNick(Utils.fromHtml(matcher.group(4)));
        notificationEvent.setTimeStamp(Integer.parseInt(matcher.group(5)));
        notificationEvent.setMessageCount(Integer.parseInt(matcher.group(6)));
        if (notificationEvent.getUserNick().isEmpty() && notificationEvent.getThemeId() == 0) {
            notificationEvent.setUserNick("Сообщения 4PDA");
        }
        return notificationEvent;
    }
}
