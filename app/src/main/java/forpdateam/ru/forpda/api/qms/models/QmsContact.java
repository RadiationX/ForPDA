package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsContact;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsContact extends RealmObject implements IQmsContact {

    @PrimaryKey
    private String nick;
    private String avatar;
    private int id, count;

    public QmsContact() {
    }

    @Override
    public String getNick() {
        return nick;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getCount() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
