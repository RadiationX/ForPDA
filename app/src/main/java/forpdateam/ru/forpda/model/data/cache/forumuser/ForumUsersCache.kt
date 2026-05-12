package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.entity.db.ForumUserBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.db.ForumUsersDao

/**
 * Created by radiationx on 08.07.17.
 */

class ForumUsersCache(
    private val userSource: UserSource,
    private val forumUsersDao: ForumUsersDao
) {

    suspend fun saveUser(forumUser: ForumUser) {
        forumUsersDao.upsert(forumUser.toDb())
    }

    suspend fun saveUsers(forumUsers: List<ForumUser>) {
        forumUsersDao.upsertAll(forumUsers.map { it.toDb() })
    }

    suspend fun getUserById(id: Int): ForumUser? {
        return forumUsersDao.getById(id)?.toDomain()
    }

    suspend fun getUserByNick(nick: String): ForumUser? {
        val user = forumUsersDao.getByNick(nick)?.toDomain()
        userSource.findUsers(nick).getOrNull(0)?.also { foundUser ->
            saveUser(foundUser)
        }
        return user
    }

}

fun ForumUserBd.toDomain(): ForumUser {
    return ForumUser.required(id, nick, avatar)
}

fun ForumUser.toDb(): ForumUserBd {
    return ForumUserBd(id, nick, avatar)
}