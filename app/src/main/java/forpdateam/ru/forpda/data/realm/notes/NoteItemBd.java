package forpdateam.ru.forpda.data.realm.notes;

import forpdateam.ru.forpda.data.models.notes.INoteItem;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 06.09.17.
 */

public class NoteItemBd extends RealmObject implements INoteItem {

    @PrimaryKey
    private long id;
    private String title;
    private String link;
    private String content;

    public NoteItemBd() {
    }

    public NoteItemBd(INoteItem item) {
        id = item.getId();
        title = item.getTitle();
        link = item.getLink();
        content = item.getContent();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }
}
