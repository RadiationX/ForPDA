package forpdateam.ru.forpda.presentation.forum;

import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 03.01.18.
 */

public interface ForumView extends IBaseView {

    void showForums(ForumItemTree forumRoot);
}
