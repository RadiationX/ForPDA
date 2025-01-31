package forpdateam.ru.forpda.entity.remote.news

import forpdateam.ru.forpda.entity.DeferredData

/**
 * Created by radiationx on 28.08.17.
 */

data class NewsItem(
    val id: Int,
    val authorId: Int,
    val url: String,
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val imgUrl: String,
    val commentsCount: Int,
    val tags: List<Tag>,
    val avatar: DeferredData<String>?
)
