package forpdateam.ru.forpda.entity.remote.reputation

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 20.03.17.
 */

data class RepItem(
    val user: User,
    val title: String,
    val sourceUrl: String?,
    val sourceTitle: String?,
    val image: String,
    val date: String,
)
