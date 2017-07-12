package forpdateam.ru.forpda.api.events.models;

/**
 * Created by radiationx on 10.07.17.
 */

public class NotificationEvent {
    private WebSocketEvent webSocketEvent;
    private int themeId = 0;
    private int userId = 0;
    private int timeStamp = 0;

    private String themeTitle = "";
    private String userNick = "";
    private String source = "";

    //Theme, Mentions?
    private int lastReadTimeStamp = 0;

    //Theme, Mentions?
    private boolean important = false;

    //Theme, Mentions?
    private int messageCount = 0;

    public WebSocketEvent getWebSocketEvent() {
        return webSocketEvent;
    }

    public void setWebSocketEvent(WebSocketEvent webSocketEvent) {
        this.webSocketEvent = webSocketEvent;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getThemeTitle() {
        return themeTitle;
    }

    public void setThemeTitle(String themeTitle) {
        this.themeTitle = themeTitle;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public int getLastReadTimeStamp() {
        return lastReadTimeStamp;
    }

    public void setReadTimeStamp(int lastReadTimeStamp) {
        this.lastReadTimeStamp = lastReadTimeStamp;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
