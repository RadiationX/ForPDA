package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.entity.db.ForumUserDb
import forpdateam.ru.forpda.entity.remote.others.user.ForumPostUser
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.db.ForumUsersDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by radiationx on 08.07.17.
 */

class ForumUsersCache @Inject constructor(
    private val userSource: UserSource,
    private val forumUsersDao: ForumUsersDao
) {

    suspend fun saveUser(forumUser: ForumUser) {
        forumUsersDao.upsert(forumUser.toDb())
    }

    suspend fun saveUsers(forumUsers: List<ForumUser>) {
        forumUsersDao.upsertAll(forumUsers.map { it.toDb() })
    }

    suspend fun savePostUsers(forumUsers: List<ForumPostUser>) {
        forumUsersDao.upsertAll(forumUsers.mapNotNull { it.toDb() })
    }

    fun observeUserById(id: Int): Flow<ForumUser?> {
        return forumUsersDao.observeById(id).map { it?.toDomain() }
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

fun ForumUserDb.toDomain(): ForumUser {
    return ForumUser.required(id, nick, avatar)
}

fun ForumUser.toDb(): ForumUserDb {
    return ForumUserDb(id, nick, avatar)
}

fun ForumPostUser.toDb(): ForumUserDb? {
    return avatar?.let { ForumUserDb(id, nick, it) }
}