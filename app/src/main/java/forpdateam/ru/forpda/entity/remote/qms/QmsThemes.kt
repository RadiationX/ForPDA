package forpdateam.ru.forpda.entity.remote.qms

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 21.09.16.
 */

data class QmsThemes(
    val user: User,
    val themes: List<QmsTheme>
)
