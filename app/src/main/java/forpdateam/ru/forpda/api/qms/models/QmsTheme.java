package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;
import io.realm.RealmObject;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsTheme extends RealmObject implements IQmsTheme {

    private int id;
    private int countMessages, countNew;
    private String name, date;

    public QmsTheme() {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public int getCountMessages() {
        return countMessages;
    }

    @Override
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
