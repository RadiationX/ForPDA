package forpdateam.ru.forpda.data.realm.qms;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 25.03.17.
 */

public class QmsContactBd extends RealmObject implements IQmsContact {

    @PrimaryKey
    private String nick;
    private String avatar;
    private int id, count;

    public QmsContactBd() {
    }

    public QmsContactBd(QmsContact contact) {
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
