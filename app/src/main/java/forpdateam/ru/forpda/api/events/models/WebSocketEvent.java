package forpdateam.ru.forpda.api.events.models;

/**
 * Created by radiationx on 10.07.17.
 */

public class WebSocketEvent {
    public final static int SITE_EVENT_NEW  = 1;
    public final static int SITE_EVENT_READ = 2;
    public final static int SITE_EVENT_MENTION = 3;
    public final static int SITE_EVENT_HAT_CHANGE = 4;

    public final static String SITE_TYPE_SITE = "s";
    public final static String SITE_TYPE_THEME = "t";
    public final static String SITE_TYPE_QMS = "q";
    /*
    * New - "1" - Qms/Favorites
    * Read - "2" - Qms/Favorites
    * Mention - "3" - Site/Mentions
    * Hat change - "4" - Favorites
    * */
    public final static int EVENT_NEW = 1;
    public final static int EVENT_READ = 2;
    public final static int EVENT_MENTION = 3;
    public final static int EVENT_HAT_CHANGE = 4;

    /*
    * Site - "s"
    * Favorites/Mentions/Themes - "t"
    * Qms chat - "q"
    * */
    public final static int TYPE_SITE = 11;
    public final static int TYPE_THEME = 12;
    public final static int TYPE_QMS = 13;

    /* Unknown field, default: 30309 */
    private int unknown1 = 0;

    /* Unknown field, default: 0 */
    private int unknown2 = 0;

    /* Type: "s"|"t"|"q" */
    private int type = 0;

    /* Theme themeId: Qms|Site|Fav| */
    private int id = 0;

    /* Code: 1|2|3|4 */
    private int eventCode = 0;

    /*
    * QMS, Mention: message/post themeId;
    * Fav: timestamp
    * */
    private int messageId = 0;

    public int getUnknown1() {
        return unknown1;
    }

    public void setUnknown1(int unknown1) {
        this.unknown1 = unknown1;
    }

    public int getUnknown2() {
        return unknown2;
    }

    public void setUnknown2(int unknown2) {
        this.unknown2 = unknown2;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String typeName() {
        switch (type) {
            case TYPE_SITE:
                return "site";
            case TYPE_THEME:
                return "theme";
            case TYPE_QMS:
                return "qms";
        }
        return "unknown";
    }

    public String codeName() {
        switch (eventCode) {
            case EVENT_NEW:
                return "new";
            case EVENT_READ:
                return "read";
            case EVENT_MENTION:
                return "mention";
            case EVENT_HAT_CHANGE:
                return "hat_change";
        }
        return "unknown";
    }

    public int createNotificationId() {
        return createNotificationId(eventCode);
    }

    public int createNotificationId(int eventCodeArg) {
        return (id / 4) + type + eventCodeArg;
    }

    @Override
    public String toString() {
        return "WebSocketEvent {" + "unk1=" + unknown1 + ", unk2=" + unknown2 + "; type=" + typeName() + ", code=" + codeName() + ", themeId=" + id + ", messId=" + messageId + "}";
    }
}
