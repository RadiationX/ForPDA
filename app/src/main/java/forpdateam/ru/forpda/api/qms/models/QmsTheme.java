package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsThread;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsTheme implements IQmsThread {
    String id, name, date, countMessages, countNew;

    @Override
    public String getId() {
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
    public String getCountMessages() {
        return countMessages;
    }

    @Override
    public String getCountNew() {
        return countNew;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCountMessages(String countMessages) {
        this.countMessages = countMessages;
    }

    public void setCountNew(String countNew) {
        this.countNew = countNew;
    }
}
