package forpdateam.ru.forpda.model.data.cache.forumuser

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

interface UserSource {
    fun getUsers(nick: String): List<ForumUser>
}