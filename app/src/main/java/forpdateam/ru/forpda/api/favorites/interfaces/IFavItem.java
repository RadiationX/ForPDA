package forpdateam.ru.forpda.api.favorites.interfaces;

/**
 * Created by radiationx on 25.03.17.
 */

public interface IFavItem {
    int getFavId();

    int getTopicId();

    int getForumId();

    int getAuthorId();

    int getLastUserId();

    int getStParam();

    int getPages();

    String getDesc();

    String getTrackType();

    String getInfo();

    String getInfoColor();

    String getTopicTitle();

    String getForumTitle();

    String getAuthorUserNick();

    String getLastUserNick();

    String getDate();

    boolean isPin();

    boolean isNewMessages();

    boolean isForum();

    void setFavId(int favId);

    void setTopicId(int topicId);

    void setForumId(int forumId);

    void setAuthorId(int authorId);

    void setLastUserId(int lastUserId);

    void setStParam(int stParam);

    void setPages(int pages);

    void setDesc(String desc);

    void setTrackType(String trackType);

    void setInfo(String info);

    void setInfoColor(String infoColor);

    void setTopicTitle(String topicTitle);

    void setForumTitle(String forumTitle);

    void setAuthorUserNick(String authorUserNick);

    void setLastUserNick(String lastUserNick);

    void setDate(String date);

    void setPin(boolean pin);

    void setNewMessages(boolean newMessages);

    void setForum(boolean isForum);
}
