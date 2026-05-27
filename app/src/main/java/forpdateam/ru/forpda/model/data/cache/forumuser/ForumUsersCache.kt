package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.entity.db.ForumUserDb
import forpdateam.ru.forpda.entity.remote.others.user.ForumPostUser
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.db.ForumUsersDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.coretypes.UserId
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

    fun observeUserById(id: UserId): Flow<ForumUser?> {
        return forumUsersDao.observeById(id.id).map { it?.toDomain() }
    }

    suspend fun getUserById(id: UserId): ForumUser? {
        val user = forumUsersDao.getById(id.id)?.toDomain()
        if (user != null) {
            return user
        }
        return coRunCatching {
            userSource.findUser(id)
        }.onSuccess {
            saveUser(it)
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

    suspend fun getUserByNick(nick: String): ForumUser? {
        val user = forumUsersDao.getByNick(nick)?.toDomain()
        if (user != null) {
            return user
        }
        return coRunCatching {
            userSource.findUsers(nick).find { it.nick.equals(nick, ignoreCase = true) }
        }.onSuccess {
            if (it != null) {
                saveUser(it)
            }
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

}

fun ForumUserDb.toDomain(): ForumUser {
    return ForumUser(UserId(id), nick, avatar)
}

fun ForumUser.toDb(): ForumUserDb {
    return ForumUserDb(id.id, nick, avatar)
}

fun ForumPostUser.toDb(): ForumUserDb? {
    return avatar?.let { ForumUserDb(id.id, nick, it) }
}