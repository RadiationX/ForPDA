package forpdateam.ru.forpda.api.favorites.models;

import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavItem implements IFavItem {
    private int favId;
    private int topicId, forumId, authorId, lastUserId, stParam, pages;
    private String trackType, info, infoColor, topicTitle, forumTitle, authorUserNick, lastUserNick, date, desc;
    private boolean pin = false, isNewMessages = false;

    public FavItem() {
    }

    public FavItem(IFavItem item){
        favId = item.getFavId();
        topicId = item.getTopicId();
        forumId = item.getForumId();
        authorId = item.getAuthorId();
        lastUserId = item.getLastUserId();
        stParam = item.getStParam();
        pages = item.getPages();

        trackType = item.getTrackType();
        info = item.getInfo();
        infoColor = item.getInfoColor();
        topicTitle = item.getTopicTitle();
        forumTitle = item.getForumTitle();
        authorUserNick = item.getAuthorUserNick();
        lastUserNick = item.getLastUserNick();
        date = item.getDate();
        desc = item.getDesc();

        pin = item.isPin();
        isNewMessages = item.isNewMessages();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getFavId() {
        return favId;
    }

    public void setFavId(int favId) {
        this.favId = favId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(int lastUserId) {
        this.lastUserId = lastUserId;
    }

    public int getStParam() {
        return stParam;
    }

    public void setStParam(int stParam) {
        this.stParam = stParam;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfoColor() {
        return infoColor;
    }

    public void setInfoColor(String infoColor) {
        this.infoColor = infoColor;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getForumTitle() {
        return forumTitle;
    }

    public void setForumTitle(String forumTitle) {
        this.forumTitle = forumTitle;
    }

    public String getAuthorUserNick() {
        return authorUserNick;
    }

    public void setAuthorUserNick(String authorUserNick) {
        this.authorUserNick = authorUserNick;
    }

    public String getLastUserNick() {
        return lastUserNick;
    }

    public void setLastUserNick(String lastUserNick) {
        this.lastUserNick = lastUserNick;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPin() {
        return pin;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }

    public boolean isNewMessages() {
        return isNewMessages;
    }

    public void setNewMessages(boolean newMessages) {
        isNewMessages = newMessages;
    }
}
