package forpdateam.ru.forpda.entity.db.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "favorites")
class FavItemBd(
    @PrimaryKey
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
