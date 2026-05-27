package forpdateam.ru.forpda.entity.remote.news

import forpdateam.ru.forpda.entity.DeferredData
import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.ArticleId

/**
 * Created by radiationx on 28.08.17.
 */

data class NewsItem(
    val id: ArticleId,
    val author: User,
    val url: String,
    val title: String,
    val description: String,
    val date: String,
    val imgUrl: String,
    val commentsCount: Int,
    val tags: List<Tag>,
    val avatar: DeferredData<String>?
)
