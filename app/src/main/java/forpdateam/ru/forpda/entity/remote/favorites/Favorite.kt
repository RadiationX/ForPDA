package forpdateam.ru.forpda.entity.remote.favorites

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 22.09.16.
 */

sealed interface Favorite {
    val favId: Int
    val title: String
    val trackType: String
    val isPin: Boolean
    val isNew: Boolean
    val date: String

    data class Topic(
        override val favId: Int,
        val topicId: Int,
        override val title: String,
        override val trackType: String,
        override val isPin: Boolean,
        override val isNew: Boolean,
        val isPoll: Boolean,
        val isClosed: Boolean,
        val stParam: Int?,
        val desc: String?,
        val forumId: Int,
        val forumTitle: String,
        val author: User,
        val lastUser: User,
        override val date: String,
        val curator: User?,
    ) : Favorite

    data class Forum(
        override val favId: Int,
        val forumId: Int,
        override val title: String,
        override val trackType: String,
        override val isPin: Boolean,
        override val isNew: Boolean,
        override val date: String,
        val lastUser: User?,
    ) : Favorite
}
