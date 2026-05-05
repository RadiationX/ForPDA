package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.queryEquals
import forpdateam.ru.forpda.entity.db.ForumUserBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

/**
 * Created by radiationx on 08.07.17.
 */

class ForumUsersCache(
    private val userSource: UserSource,
    private val realm: RealmWrapper
) {

    suspend fun saveUser(forumUser: ForumUser) {
        realm.write {
            upsert(forumUser.toDb())
        }
    }

    suspend fun saveUsers(forumUsers: List<ForumUser>) {
        realm.write {
            upsertAll(forumUsers.map { it.toDb() })
        }
    }

    suspend fun getUserById(id: Int): ForumUser? {
        return realm
            .queryEquals<ForumUserBd>("id", id)
            .mapFirst { it.toDomain() }
    }

    suspend fun getUserByNick(nick: String): ForumUser? {
        val user = realm
            .queryEquals<ForumUserBd>("nick", nick)
            .mapFirst { it.toDomain() }
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