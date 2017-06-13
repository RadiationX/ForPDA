package forpdateam.ru.forpda.api.qms.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 20.09.16.
 */

public class QmsChatModel {
    private int themeId = 0;
    private int userId = 0;
    private int showedMessIndex = 0;
    private String title;
    private String nick;
    private String avatarUrl;
    private String html;
    private List<QmsMessage> messages = new ArrayList<>();

    public void addMessage(QmsMessage item) {
        messages.add(item);
    }

    public List<QmsMessage> getMessages() {
        return messages;
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
