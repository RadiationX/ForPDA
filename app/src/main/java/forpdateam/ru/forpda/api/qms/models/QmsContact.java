package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsContact implements IQmsContact {
    private String nick;
    private String avatar;
    private int id, count;

    public QmsContact() {
    }

    public QmsContact(IQmsContact contact) {
        nick = contact.getNick();
        avatar = contact.getAvatar();
        id = contact.getId();
        count = contact.getCount();
    }

    public String getNick() {
        return nick;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
