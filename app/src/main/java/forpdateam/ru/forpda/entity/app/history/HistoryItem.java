package forpdateam.ru.forpda.entity.app.history;

/**
 * Created by radiationx on 01.01.18.
 */

public class HistoryItem implements IHistoryItem {
    private int id;
    private String url;
    private String date;
    private String title;
    private long unixTime;

    public HistoryItem() {
    }

    public HistoryItem(IHistoryItem item) {
        this.id = item.getId();
        this.url = item.getUrl();
        this.date = item.getDate();
        this.title = item.getTitle();
        this.unixTime = item.getUnixTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }
}
