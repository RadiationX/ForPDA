package forpdateam.ru.forpda.api.news.models;

import java.util.ArrayList;

/**
 * Created by radiationx on 02.09.17.
 */

public class Comment {
    private int id;
    private int userId;
    private String userNick;
    private String date;
    private String content;
    private boolean deleted = false;
    private boolean canReply = false;
    private ArrayList<Comment> children = new ArrayList<>();
    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ArrayList<Comment> getChildren() {
        return children;
    }

    public void addChild(Comment child) {
        children.add(child);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
