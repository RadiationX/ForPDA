package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
sealed interface QmsMessage {
    data class Date(
        var date: String? = null
    ) : QmsMessage

    data class Regular(
        var isMyMessage: Boolean = false,
        var id: Int = 0,
        var readStatus: Boolean = false,
        var time: String? = null,
        var avatar: String? = null,
        var content: String? = null
    ) : QmsMessage
}

fun QmsMessage.asRegular(): QmsMessage.Regular? {
    return this as? QmsMessage.Regular
}
