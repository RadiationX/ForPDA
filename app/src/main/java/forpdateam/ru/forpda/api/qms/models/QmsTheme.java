package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsTheme;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsTheme implements IQmsTheme {
    private int id, userId;
    private int countMessages, countNew;
    private String name, date, nick;

    public QmsTheme() {
    }

    public QmsTheme(IQmsTheme qmsTheme) {
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
