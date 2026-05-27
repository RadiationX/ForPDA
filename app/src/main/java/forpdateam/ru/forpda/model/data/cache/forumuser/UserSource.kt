package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import ru.radiationx.coretypes.UserId

interface UserSource {
    suspend fun findUser(id: UserId): ForumUser
    suspend fun findUsers(nick: String): List<ForumUser>
}