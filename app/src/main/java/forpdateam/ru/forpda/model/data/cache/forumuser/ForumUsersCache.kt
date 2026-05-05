package forpdateam.ru.forpda.model.data.cache.forumuser

import android.util.Log
import forpdateam.ru.forpda.entity.db.ForumUserBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import io.realm.Realm

/**
 * Created by radiationx on 08.07.17.
 */

class ForumUsersCache(
    private val userSource: UserSource
) {

    suspend fun saveUser(forumUser: ForumUser) = saveUsers(listOf(forumUser))

    suspend fun saveUsers(forumUsers: List<ForumUser>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realm ->
            realm.insertOrUpdate(forumUsers.map { user ->
                Log.e("kekosina", "saveUser  ${user.id}, ${user.nick}")
                user.toDb()
            })
        }
    }


    suspend fun getUserById(id: Int): ForumUser? = Realm.getDefaultInstance().use {
        it.where(ForumUserBd::class.java).equalTo("id", id).findFirst()?.toDomain()
    }

    suspend fun getUserByNick(nick: String): ForumUser? = Realm.getDefaultInstance().use {
        it.where(ForumUserBd::class.java).equalTo("nick", nick).findFirst()
            ?.toDomain()
            ?: userSource.getUsers(nick).getOrNull(0)?.also { user ->
                saveUser(user)
            }
    }

}

fun ForumUserBd.toDomain(): ForumUser {
    return ForumUser.required(id, nick, avatar)
}

fun ForumUser.toDb(): ForumUserBd {
    return ForumUserBd(id, nick, avatar)
}