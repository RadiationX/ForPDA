package forpdateam.ru.forpda.entity.db.favorites

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import forpdateam.ru.forpda.entity.db.UserDb
import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.UserId

@Entity(tableName = "favorite_topics")
data class FavoriteTopicDb(
    @PrimaryKey
    @ColumnInfo("fav_id") val favId: Int,
    @ColumnInfo("topic_id") val topicId: Int,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("track_type") val trackType: String,
    @ColumnInfo("is_pin") val isPin: Boolean,
    @ColumnInfo("is_new") val isNew: Boolean,
    @ColumnInfo("is_poll") val isPoll: Boolean,
    @ColumnInfo("is_closed") val isClosed: Boolean,
    @ColumnInfo("st_param") val stParam: Int?,
    @ColumnInfo("desc") val desc: String?,
    @ColumnInfo("forum_id") val forumId: Int,
    @ColumnInfo("forum_title") val forumTitle: String,
    @Embedded("author_") val author: UserDb,
    @Embedded("last_user_") val lastUser: UserDb,
    @ColumnInfo("last_date") val date: String,
    @Embedded("curator_") val curator: UserDb?,
)