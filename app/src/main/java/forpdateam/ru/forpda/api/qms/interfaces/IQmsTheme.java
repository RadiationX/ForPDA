package forpdateam.ru.forpda.api.qms.interfaces;

/**
 * Created by radiationx on 03.08.16.
 */
public interface IQmsTheme {
    int getId();

    String getName();

    String getDate();

    int getCountMessages();

    int getCountNew();

    void setId(int id);

    void setName(String name);

    void setDate(String date);

    void setCountMessages(int countMessages);

    void setCountNew(int countNew);
}
