package forpdateam.ru.forpda.entity.remote.search

import forpdateam.ru.forpda.entity.remote.BaseForumPost

/**
 * Created by radiationx on 01.02.17.
 */

class SearchItem : BaseForumPost(), ISearchItem {
    override var title: String? = null
    override var desc: String? = null
    override var imageUrl: String? = null
}
