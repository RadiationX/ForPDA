package forpdateam.ru.forpda.entity.remote.search

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.TopicId

/**
 * Created by radiationx on 01.02.17.
 */

sealed interface SearchItem {

    data class News(
        val id: ArticleId,
        val imageUrl: String,
        val date: String,
        val user: User,
        val title: String,
        val body: String,
    ) : SearchItem

    data class Topic(
        val topicId: TopicId,
        val title: String,
        val desc: String,
        val forumId: ForumId,
        val user: User,
        val date: String,
    ) : SearchItem

    data class Post(
        val title: String,
        val post: ForumPost
    ) : SearchItem
}
