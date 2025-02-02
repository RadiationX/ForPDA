package forpdateam.ru.forpda.entity.remote.qms

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 03.08.16.
 */
data class QmsTheme(
    val id: Int,
    val countMessages: Int,
    val countNew: Int,
    val name: String?,
    val date: String?,
    // from parent
    val user: User
)
