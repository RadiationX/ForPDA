package forpdateam.ru.forpda.entity.remote.favorites

/**
 * Created by radiationx on 22.09.16.
 */

data class FavItem(
    val favId: Int,
    val topicId: Int,
    val forumId: Int,
    val authorId: Int,
    val lastUserId: Int,
    val stParam: Int,
    val pages: Int,
    val curatorId: Int,
    val trackType: String?,
    val infoColor: String?,
    val topicTitle: String?,
    val forumTitle: String?,
    val authorUserNick: String?,
    val lastUserNick: String?,
    val date: String?,
    val desc: String?,
    val curatorNick: String?,
    val subType: String?,
    val isPin: Boolean,
    val isForum: Boolean,
    val isNew: Boolean,
    val isPoll: Boolean,
    val isClosed: Boolean,
)
