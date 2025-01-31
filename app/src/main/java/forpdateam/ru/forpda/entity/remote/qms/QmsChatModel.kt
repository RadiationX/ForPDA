package forpdateam.ru.forpda.entity.remote.qms

import forpdateam.ru.forpda.entity.DeferredData

/**
 * Created by radiationx on 20.09.16.
 */

data class QmsChatModel(
    val themeId: Int,
    val userId: Int,
    val title: String,
    val nick: String,
    val avatarUrl: String,
    val messages: List<QmsMessage>,
    val showedMessIndex: Int,
    val html: DeferredData<String>?
) {

    companion object {
        const val NOT_CREATED = -1
    }
}
