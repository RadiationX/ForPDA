package forpdateam.ru.forpda.entity.remote.favorites

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 22.09.16.
 */

sealed interface Favorite {


    data class Topic(
        val favId: Int,
        val topicId: Int,
        val title: String,
        val trackType: String,
        val isPin: Boolean,
        val isNew: Boolean,
        val isPoll: Boolean,
        val isClosed: Boolean,
        val stParam: Int?,
        val desc: String?,
        val forumId: Int,
        val forumTitle: String,
        val author: User,
        val lastUser: User,
        val date: String,
        val curator: User?,
    ) : Favorite

    data class Forum(
        val favId: Int,
        val forumId: Int,
        val title: String,
        val trackType: String,
        val isPin: Boolean,
        val isNew: Boolean,
        val date: String,
        val lastUser: User?,
    ) : Favorite
}
