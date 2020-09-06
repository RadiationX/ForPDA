package forpdateam.ru.forpda.entity.remote.qms

import java.util.ArrayList

/**
 * Created by radiationx on 20.09.16.
 */

class QmsChatModel {
    var themeId = NOT_CREATED
    var userId = NOT_CREATED
    var showedMessIndex = 0
    var title: String? = null
    var nick: String? = null
    var avatarUrl: String? = null
    var html: String? = null
    val messages = mutableListOf<QmsMessage>()

    companion object {
        const val NOT_CREATED = -1
    }
}
