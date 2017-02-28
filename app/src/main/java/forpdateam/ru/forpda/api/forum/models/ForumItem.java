package forpdateam.ru.forpda.api.forum.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumItem {
    private List<ForumItem> forums;
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

    public List<ForumItem> getForums() {
        return forums;
    }

    public void addForum(ForumItem item) {
        if (forums == null)
            forums = new ArrayList<>();
        forums.add(item);
    }
}
