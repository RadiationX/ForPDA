package forpdateam.ru.forpda.api.qms.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 20.09.16.
 */

public class QmsChatModel {
    private List<QmsMessage> chatItemsList = new ArrayList<>();
    private int themeId, userId, showedMessIndex;
    private String title, nick, avatarUrl, html;

    public void addChatItem(QmsMessage item) {
        chatItemsList.add(item);
    }

    public List<QmsMessage> getChatItemsList() {
        return chatItemsList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public int getShowedMessIndex() {
        return showedMessIndex;
    }

    public void setShowedMessIndex(int showedMessIndex) {
        this.showedMessIndex = showedMessIndex;
    }
}
