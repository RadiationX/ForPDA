package forpdateam.ru.forpda.entity.remote.qms

import forpdateam.ru.forpda.entity.DeferredData
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

/**
 * Created by radiationx on 20.09.16.
 */

data class QmsChatModel(
    val themeId: Int,
    val user: ForumUser,
    val title: String,
    val messages: List<QmsMessage>,
    val showedMessIndex: Int,
    val html: DeferredData<String>?
) {

    companion object {
        const val NOT_CREATED = -1
    }
}
