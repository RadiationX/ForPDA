package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsContact implements IQmsContact {
    private String nick, id, count, avatar;
    @Override
    public String getNick() {
        return nick;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCount() {
        return count;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
