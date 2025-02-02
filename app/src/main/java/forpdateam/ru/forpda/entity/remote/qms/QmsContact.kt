package forpdateam.ru.forpda.entity.remote.qms

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

/**
 * Created by radiationx on 03.08.16.
 */
data class QmsContact(
    val user: ForumUser,
    val count: Int,
)
