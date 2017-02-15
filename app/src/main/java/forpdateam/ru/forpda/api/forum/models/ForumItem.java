package forpdateam.ru.forpda.api.forum.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 15.02.17.
 */

public class ForumItem {
    private int id = -1;
    private String title;
    private List<ForumItem> forums;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
