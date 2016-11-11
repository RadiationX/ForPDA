package forpdateam.ru.forpda.api.qms.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 20.09.16.
 */

public class QmsChatModel {
    private List<QmsMessage> chatItemsList = new ArrayList<>();
    private String title;
    private String nick;
    private String avatarUrl;

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
}
