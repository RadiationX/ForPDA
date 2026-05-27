package forpdateam.ru.forpda.entity.remote.qms

import ru.radiationx.coretypes.QmsThreadId

/**
 * Created by radiationx on 03.08.16.
 */
data class QmsTheme(
    val id: QmsThreadId,
    val countMessages: Int,
    val countNew: Int,
    val name: String,
    val date: String,
)
