package forpdateam.ru.forpda.api.mentions.models;

/**
 * Created by radiationx on 21.01.17.
 */

public class MentionItem {
    public final static int STATE_UNREAD = 0;
    public final static int STATE_READ = 1;
    public final static int TYPE_FORUM = 0;
    public final static int TYPE_NEWS = 1;
    private String title, desc, link, date, nick;
    private int state = STATE_READ, type = TYPE_FORUM;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
