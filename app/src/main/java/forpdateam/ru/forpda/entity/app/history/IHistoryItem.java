package forpdateam.ru.forpda.entity.app.history;

/**
 * Created by radiationx on 01.01.18.
 */

public interface IHistoryItem {
    int getId();

    String getUrl();

    String getDate();

    String getTitle();

    long getUnixTime();

    void setId(int id);

    void setUrl(String url);

    void setDate(String date);

    void setTitle(String title);

    void setUnixTime(long unixTime);
}
