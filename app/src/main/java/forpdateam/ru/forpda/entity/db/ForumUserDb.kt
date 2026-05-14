package forpdateam.ru.forpda.entity.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
@Entity("forum_users")
data class ForumUserDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("nick") val nick: String,
    @ColumnInfo("avatar_url") val avatar: String
) 
