package forpdateam.ru.forpda.api.forum.models;

/**
 * Created by radiationx on 17.10.17.
 */

public class Announce {
    private int id;
    private int forumId;
    private String title;
    private String html;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
