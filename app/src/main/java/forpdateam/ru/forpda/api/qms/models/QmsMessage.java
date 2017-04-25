package forpdateam.ru.forpda.api.qms.models;

import forpdateam.ru.forpda.api.qms.interfaces.IQmsChatItem;

/**
 * Created by radiationx on 03.08.16.
 */
public class QmsMessage implements IQmsChatItem {
    private boolean myMessage = false;
    private boolean isDate = false;
    private int id;
    private boolean readStatus = false;
    private String time, avatar, date, content;

    @Override
    public boolean isMyMessage() {
        return myMessage;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean getReadStatus() {
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
    public String getContent() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMyMessage(boolean myMessage) {
        this.myMessage = myMessage;
    }
}
