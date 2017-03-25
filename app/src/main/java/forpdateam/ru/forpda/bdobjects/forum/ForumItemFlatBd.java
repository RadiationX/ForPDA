package forpdateam.ru.forpda.bdobjects.forum;

import forpdateam.ru.forpda.api.forum.interfaces.IForumItemFlat;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import io.realm.RealmObject;

/**
 * Created by radiationx on 25.03.17.
 */

public class ForumItemFlatBd extends RealmObject implements IForumItemFlat {
    private int id = -1;
    private int parentId = -1, level = -1;
    private String title;

    public ForumItemFlatBd() {
    }

    public ForumItemFlatBd(ForumItemTree item) {
        id = item.getId();
        parentId = item.getParentId();
        title = item.getTitle();
        level = item.getLevel();
    }

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
