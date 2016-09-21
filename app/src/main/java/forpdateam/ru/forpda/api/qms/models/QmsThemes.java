package forpdateam.ru.forpda.api.qms.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 21.09.16.
 */

public class QmsThemes {
    private List<QmsTheme> themes = new ArrayList<>();
    private String nick;

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
}
