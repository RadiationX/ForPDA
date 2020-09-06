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

    private val requestsInSession = mutableSetOf<String>()

    fun saveUser(forumUser: ForumUser) = saveUsers(listOf(forumUser))

    fun saveUsers(forumUsers: List<ForumUser>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realm ->
            realm.insertOrUpdate(forumUsers.map {
                Log.e("kekosina", "saveUser  ${it.id}, ${it.nick}")
                ForumUserBd(it)
            })
        }
    }


    fun getUserById(id: Int): ForumUser? = Realm.getDefaultInstance().use {
        it.where(ForumUserBd::class.java).equalTo("id", id).findFirst()?.let { ForumUser(it) }
    }

    fun getUserByNick(nick: String): ForumUser? = Realm.getDefaultInstance().use {
        it.where(ForumUserBd::class.java).equalTo("nick", nick).findFirst()
                ?.let { ForumUser(it) }
                ?: userSource.getUsers(nick).getOrNull(0)?.also {
                    saveUser(it)
                }
    }

}
