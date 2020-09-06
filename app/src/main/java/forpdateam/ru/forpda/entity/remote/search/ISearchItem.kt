package forpdateam.ru.forpda.entity.remote.search

import forpdateam.ru.forpda.entity.remote.IBaseForumPost

/**
 * Created by radiationx on 27.04.17.
 */

interface ISearchItem : IBaseForumPost {
    val imageUrl: String?
    val title: String?
    val desc: String?
}
