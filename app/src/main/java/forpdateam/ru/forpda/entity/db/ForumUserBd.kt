package forpdateam.ru.forpda.entity.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
@Entity("forum_users")
data class ForumUserBd(
    @PrimaryKey
    val id: Int,
    val nick: String?,
    val avatar: String?
) 
