package forpdateam.ru.forpda.api.qms.interfaces;

import android.text.Spanned;

/**
 * Created by radiationx on 03.08.16.
 */
public interface IQmsChatItem {
    boolean getWhoseMessage();

    String getId();

    String getReadStatus();

    String getTime();

    String getAvatar();

    String getContent();

    boolean isDate();

    String getDate();
}
