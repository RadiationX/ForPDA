package forpdateam.ru.forpda.entity.app.notes;

/**
 * Created by radiationx on 06.09.17.
 */

public interface INoteItem {

    long getId();

    String getTitle();

    String getLink();

    String getContent();

    void setId(long id);

    void setTitle(String title);

    void setLink(String link);

    void setContent(String content);

}
