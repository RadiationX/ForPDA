package forpdateam.ru.forpda.model.data.providers

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.model.data.cache.forumuser.UserSource
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import javax.inject.Inject

class UserSourceProvider @Inject constructor(
    private val qmsApi: QmsApi
) : UserSource {
    override suspend fun findUsers(nick: String): List<ForumUser> = qmsApi.findUser(nick)
}