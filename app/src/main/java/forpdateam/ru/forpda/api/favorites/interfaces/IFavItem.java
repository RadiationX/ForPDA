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

    int getCuratorId();

    String getDesc();

    String getTrackType();

    String getInfoColor();

    String getTopicTitle();

    String getForumTitle();

    String getAuthorUserNick();

    String getLastUserNick();

    String getDate();

    String getCuratorNick();

    String getSubType();

    boolean isPin();

    boolean isForum();

    boolean isNew();

    boolean isPoll();

    boolean isClosed();

    void setFavId(int favId);

    void setTopicId(int topicId);

    void setForumId(int forumId);

    void setAuthorId(int authorId);

    void setLastUserId(int lastUserId);

    void setStParam(int stParam);

    void setPages(int pages);

    void setCuratorId(int curatorId);

    void setDesc(String desc);

    void setTrackType(String trackType);

    void setInfoColor(String infoColor);

    void setTopicTitle(String topicTitle);

    void setForumTitle(String forumTitle);

    void setAuthorUserNick(String authorUserNick);

    void setLastUserNick(String lastUserNick);

    void setDate(String date);

    void setCuratorNick(String curatorNick);

    void setSubType(String subType);

    void setPin(boolean pin);

    void setForum(boolean isForum);

    void setNew(boolean aNew);

    void setPoll(boolean poll);

    void setClosed(boolean closed);

}
