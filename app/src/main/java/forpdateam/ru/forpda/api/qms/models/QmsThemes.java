package forpdateam.ru.forpda.api.qms.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 21.09.16.
 */

public class QmsThemes extends RealmObject {
    @PrimaryKey
    private int userId;
    private String nick;
    private RealmList<QmsTheme> themes = new RealmList<>();

    public QmsThemes() {
    }

    public RealmList<QmsTheme> getThemes() {
        return themes;
    }

    public void addTheme(QmsTheme theme) {
        themes.add(theme);
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
