package forpdateam.ru.forpda.model.repository.avatar

import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class AvatarRepository(
    private val forumUsersCache: ForumUsersCache,
    private val schedulers: SchedulersProvider
) : BaseRepository(schedulers) {

    fun getAvatar(id: Int, nick: String): String {
        return getAvatarSync(id, nick) ?: throw NullPointerException("No avatar/user by id: $id")
    }

    fun getAvatar(id: Int): String {
        return getAvatarSync(id) ?: throw NullPointerException("No avatar/user by id: $id")
    }

    fun getAvatar(nick: String): String {
        return getAvatarSync(nick) ?: throw NullPointerException("No avatar/user by nick: $nick")
    }

    fun getAvatarSync(id: Int, nick: String): String? {
        val forumUser = forumUsersCache.getUserById(id)
            ?: forumUsersCache.getUserByNick(nick)
        return forumUser?.avatar
    }

    fun getAvatarSync(id: Int): String? = forumUsersCache.getUserById(id)?.avatar

    fun getAvatarSync(nick: String): String? = forumUsersCache.getUserByNick(nick)?.avatar

}
