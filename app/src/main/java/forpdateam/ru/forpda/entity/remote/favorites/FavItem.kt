package forpdateam.ru.forpda.entity.remote.favorites

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 22.09.16.
 */

data class FavItem(
    val favId: Int,
    val topicId: Int,
    val forumId: Int,
    val author: User,
    val lastUser: User,
    val curator: User?,
    val stParam: Int,
    val pages: Int,
    val trackType: String?,
    val infoColor: String?,
    val topicTitle: String?,
    val forumTitle: String?,
    val date: String?,
    val desc: String?,
    val subType: String?,
    val isPin: Boolean,
    val isForum: Boolean,
    val isNew: Boolean,
    val isPoll: Boolean,
    val isClosed: Boolean,
)
