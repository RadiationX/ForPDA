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
    private boolean collapsed = false;
    private boolean canReply = false;
    private ArrayList<Comment> children = new ArrayList<>();
    private int level;
    private Karma karma;

    public Comment() {
    }

    public Comment(Comment comment) {
        this.id = comment.getId();
        this.userId = comment.getUserId();
        this.userNick = comment.getUserNick();
        this.date = comment.getDate();
        this.content = comment.getContent();
        this.deleted = comment.isDeleted();
        this.canReply = comment.canReply;
        this.level = comment.getLevel();
        this.collapsed = comment.isCollapsed();
        this.karma = comment.getKarma();
    }

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

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
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

    public Karma getKarma() {
        return karma;
    }

    public void setKarma(Karma karma) {
        this.karma = karma;
    }

    public static class Karma {
        public final static int NOT_LIKED = 0;
        public final static int LIKED = 1;
        public final static int DISLIKED = -1;
        public final static int FORBIDDEN = 2;

        private int status;
        private int count;
        private int unknown1;
        private int unknown2;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
