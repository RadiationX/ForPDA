package forpdateam.ru.forpda.presentation.mentions;

import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 01.01.18.
 */

public interface MentionsView extends IBaseView {
    void showMentions(MentionsData data);
}
