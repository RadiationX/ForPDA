package forpdateam.ru.forpda.data.realm.history;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by radiationx on 06.09.17.
 */

public class HistoryItemBd extends RealmObject {
    @PrimaryKey
    private int id;
    private String url;
    private String date;
    private String title;
    private long unixTime;


    public HistoryItemBd() {
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
