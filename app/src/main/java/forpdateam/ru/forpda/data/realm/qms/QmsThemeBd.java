package forpdateam.ru.forpda.data.realm.qms;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 25.03.17.
 */

public class QmsThemeBd extends RealmObject implements IQmsTheme {
    @PrimaryKey
    private int id;
    private int countMessages, countNew;
    private String name, date;

    public QmsThemeBd() {
    }

    public QmsThemeBd(IQmsTheme qmsTheme) {
        id = qmsTheme.getId();
        countMessages = qmsTheme.getCountMessages();
        countNew = qmsTheme.getCountNew();
        name = qmsTheme.getName();
        date = qmsTheme.getDate();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public int getCountMessages() {
        return countMessages;
    }

    public int getCountNew() {
        return countNew;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCountMessages(int countMessages) {
        this.countMessages = countMessages;
    }

    public void setCountNew(int countNew) {
        this.countNew = countNew;
    }
}
