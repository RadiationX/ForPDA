package forpdateam.ru.forpda.model.repository.avatar

import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class AvatarRepository @Inject constructor(
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getAvatar(id: UserId): String {
        return forumUsersCache.getUserById(id)?.avatar
            ?: throw NullPointerException("No avatar/user by id: $id")
    }

    suspend fun getAvatar(nick: String): String {
        return forumUsersCache.getUserByNick(nick)?.avatar
            ?: throw NullPointerException("No avatar/user by nick: $nick")
    }

    suspend fun getAvatar(id: UserId, nick: String): String {
        val avatar = forumUsersCache.getUserById(id)?.avatar
            ?: forumUsersCache.getUserByNick(nick)?.avatar
            ?: throw NullPointerException("No avatar/user by id: $id")
        return avatar
    }
}
