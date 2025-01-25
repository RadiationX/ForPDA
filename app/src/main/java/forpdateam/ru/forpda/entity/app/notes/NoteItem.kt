package forpdateam.ru.forpda.entity.app.notes;

/**
 * Created by radiationx on 06.09.17.
 */

public class NoteItem implements INoteItem {
    private long id;
    private String title;
    private String link;
    private String content;

    public NoteItem() {
    }

    public NoteItem(INoteItem item) {
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
