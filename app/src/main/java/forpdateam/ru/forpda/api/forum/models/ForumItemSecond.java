package forpdateam.ru.forpda.api.forum.models;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumItemSecond {
    private int id = -1, parentId = -1, level = -1;
    private String title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
