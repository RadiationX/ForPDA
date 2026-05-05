package forpdateam.ru.forpda.model.repository.avatar

import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache

/**
 * Created by radiationx on 01.01.18.
 */

class AvatarRepository(
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getAvatar(id: Int): String {
        return forumUsersCache.getUserById(id)?.avatar
            ?: throw NullPointerException("No avatar/user by id: $id")
    }

    suspend fun getAvatar(nick: String): String {
        return forumUsersCache.getUserByNick(nick)?.avatar
            ?: throw NullPointerException("No avatar/user by nick: $nick")
    }

    suspend fun getAvatar(id: Int, nick: String): String {
        val avatar = forumUsersCache.getUserById(id)?.avatar
            ?: forumUsersCache.getUserByNick(nick)?.avatar
            ?: throw NullPointerException("No avatar/user by id: $id")
        return avatar
    }
}
