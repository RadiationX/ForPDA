package forpdateam.ru.forpda.model.data.providers

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.cache.forumuser.UserSource
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi

class UserSourceProvider(
        private val qmsApi: QmsApi
) : UserSource {
    override fun getUsers(nick: String): List<ForumUser> = qmsApi.findUser(nick)
}