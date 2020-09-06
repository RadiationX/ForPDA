package forpdateam.ru.forpda.entity.db.qms;

import forpdateam.ru.forpda.entity.remote.qms.IQmsThemes;
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme;
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 25.03.17.
 */

public class QmsThemesBd extends RealmObject implements IQmsThemes {
    @PrimaryKey
    private int userId;
    private String nick;
    private RealmList<QmsThemeBd> themes = new RealmList<>();

    public QmsThemesBd() {
    }

    public QmsThemesBd(QmsThemes qmsThemes) {
        userId = qmsThemes.getUserId();
        nick = qmsThemes.getNick();
        for (QmsTheme theme : qmsThemes.getThemes()) {
            themes.add(new QmsThemeBd(theme));
        }
    }

    public RealmList<QmsThemeBd> getThemes() {
        return themes;
    }

    public void addTheme(QmsThemeBd theme) {
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
