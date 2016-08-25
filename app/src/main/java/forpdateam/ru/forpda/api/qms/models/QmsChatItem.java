package forpdateam.ru.forpda.api.qms.models;

import android.text.Spanned;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsChatItem;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsChatItem implements IQmsChatItem {
    private boolean whoseMessage = false;
    private boolean isDate = false;
    private String id, readStatus, time, avatar, date;
    private Spanned content;

    @Override
    public boolean getWhoseMessage() {
        return whoseMessage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getReadStatus() {
        return readStatus;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public Spanned getContent() {
        return content;
    }

    @Override
    public boolean isDate() {
        return isDate;
    }

    @Override
    public String getDate() {
        return date;
    }

    public void setIsDate(boolean isDate) {
        this.isDate = isDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setContent(Spanned content) {
        this.content = content;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setWhoseMessage(boolean whoseMessage) {
        this.whoseMessage = whoseMessage;
    }
}
