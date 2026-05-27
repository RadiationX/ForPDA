package forpdateam.ru.forpda.entity.remote.others.user

import ru.radiationx.coretypes.UserId

/**
 * Created by radiationx on 08.07.17.
 */
data class ForumUser(
    val id: UserId,
    val nick: String,
    val avatar: String
)