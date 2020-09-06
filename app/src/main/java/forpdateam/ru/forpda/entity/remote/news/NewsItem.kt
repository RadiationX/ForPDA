package forpdateam.ru.forpda.entity.remote.news

import java.util.ArrayList

/**
 * Created by radiationx on 28.08.17.
 */

class NewsItem {
    var id: Int = 0
    var authorId = 0
    var url: String? = null
    var title: String? = null
    var description: String? = null
    var author: String? = null
    var date: String? = null
    var imgUrl: String? = null
    var commentsCount: Int = 0
    var avatar: String? = null
    val tags = mutableListOf<Tag>()

}
