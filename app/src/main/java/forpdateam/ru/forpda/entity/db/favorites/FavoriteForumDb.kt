package forpdateam.ru.forpda.entity.db.favorites

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import forpdateam.ru.forpda.entity.remote.others.user.User

@Entity(tableName = "favorite_forums")
data class FavoriteForumDb(
    @PrimaryKey
    @ColumnInfo("fav_id") val favId: Int,
    @ColumnInfo("forum_id") val forumId: Int,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("track_type") val trackType: String,
    @ColumnInfo("is_pin") val isPin: Boolean,
    @ColumnInfo("is_new") val isNew: Boolean,
    @ColumnInfo("last_date") val date: String,
    @Embedded("last_user_") val lastUser: User?,
)