package forpdateam.ru.forpda.entity.remote.search

/**
 * Created by radiationx on 01.02.17.
 */

sealed interface SearchItem {

    data class News(
        val id: Int,
        val imageUrl: String,
        val date: String,
        val userId: Int,
        val nick: String,
        val title: String,
        val body: String,
    ) : SearchItem

    data class Topic(
        val topicId: Int,
        val title: String,
        val desc: String,
        val forumId: Int,
        val userId: Int,
        val nick: String,
        val date: String,
    ) : SearchItem

    data class ForumPost(
        val title: String,
        val post: forpdateam.ru.forpda.entity.remote.ForumPost
    ) : SearchItem
}
