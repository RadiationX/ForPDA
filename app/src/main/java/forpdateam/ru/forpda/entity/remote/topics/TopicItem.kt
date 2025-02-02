package forpdateam.ru.forpda.entity.remote.topics

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 01.03.17.
 */
sealed interface TopicItem {

    data class Announce(
        val title: String,
        val url: String
    ) : TopicItem

    data class Forum(
        val id: Int,
        val title: String
    ) : TopicItem

    data class Topic(
        val id: Int,
        val title: String,
        val desc: String?,
        val date: String,
        val author: User,
        val lastUser: User,
        val curator: User?,
        val flags: TopicFlags
    ) : TopicItem
}
