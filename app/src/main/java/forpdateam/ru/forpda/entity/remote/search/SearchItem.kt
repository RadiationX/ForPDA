package forpdateam.ru.forpda.entity.remote.search

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 01.02.17.
 */

sealed interface SearchItem {

    data class News(
        val id: Int,
        val imageUrl: String,
        val date: String,
        val user: User,
        val title: String,
        val body: String,
    ) : SearchItem

    data class Topic(
        val topicId: Int,
        val title: String,
        val desc: String,
        val forumId: Int,
        val user: User,
        val date: String,
    ) : SearchItem

    data class Post(
        val title: String,
        val post: ForumPost
    ) : SearchItem
}
