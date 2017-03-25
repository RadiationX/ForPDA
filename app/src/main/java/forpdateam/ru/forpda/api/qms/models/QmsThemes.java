package forpdateam.ru.forpda.api.qms.models;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsThemes;

/**
 * Created by radiationx on 21.09.16.
 */

public class QmsThemes implements IQmsThemes {
    private int userId;
    private String nick;
    private List<QmsTheme> themes = new ArrayList<>();

    public QmsThemes() {
    }

    public List<QmsTheme> getThemes() {
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
