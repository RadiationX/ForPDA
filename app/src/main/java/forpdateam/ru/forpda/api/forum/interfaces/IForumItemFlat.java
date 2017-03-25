package forpdateam.ru.forpda.api.forum.interfaces;

/**
 * Created by radiationx on 25.03.17.
 */

public interface IForumItemFlat {

    int getId();

    int getParentId();

    String getTitle();

    int getLevel();

    void setId(int id);

    void setParentId(int parentId);

    void setTitle(String title);

    void setLevel(int level);
}
