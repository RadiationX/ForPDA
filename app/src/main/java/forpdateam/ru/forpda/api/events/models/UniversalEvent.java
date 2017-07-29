package forpdateam.ru.forpda.api.events.models;

import android.support.annotation.Nullable;

/**
 * Created by radiationx on 29.07.17.
 */

public class UniversalEvent {
    public final static int SRC_EVENT_NEW = 1;
    public final static int SRC_EVENT_READ = 2;
    public final static int SRC_EVENT_MENTION = 3;
    public final static int SRC_EVENT_HAT_CHANGE = 4;
    public final static String SRC_TYPE_SITE = "s";
    public final static String SRC_TYPE_THEME = "t";
    public final static String SRC_TYPE_QMS = "q";


    public final static int EVENT_NEW = 1;
    public final static int EVENT_READ = 2;

    public final static int SOURCE_FAVORITES = 4;
    public final static int SOURCE_MENTIONS = 4;
    public final static int SOURCE_QMS = 4;
    public final static int SOURCE_SITE = 4;


    private int messageId = 0;
    private int event = 0;
    private int source = 0;


    private int sourceId = 0;
    private int userId = 0;

    private int timeStamp = 0;
    private int lastTimeStamp = 0;

    private int unreadCount = 0;
    private boolean important = false;

    private String sourceTitle = "";

    private String userNick = "";


}
