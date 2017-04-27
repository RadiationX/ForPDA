package forpdateam.ru.forpda.api.search.interfaces;

import forpdateam.ru.forpda.api.IBaseForumPost;

/**
 * Created by radiationx on 27.04.17.
 */

public interface ISearchItem extends IBaseForumPost {
    int getTopicId();

    String getImageUrl();

    String getTitle();

    String getDesc();
}
