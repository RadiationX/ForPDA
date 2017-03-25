package forpdateam.ru.forpda.api.qms.interfaces;

/**
 * Created by radiationx on 03.08.16.
 */
public interface IQmsContact {
    String getNick();

    int getId();

    int getCount();

    String getAvatar();

    void setNick(String nick);

    void setAvatar(String avatar);

    void setId(int id);

    void setCount(int count);
}
