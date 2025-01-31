package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
sealed interface QmsMessage {
    data class Date(
        val date: String
    ) : QmsMessage

    data class Regular(
        val isMyMessage: Boolean,
        val id: Int,
        val readStatus: Boolean,
        val time: String,
        val avatar: String,
        val content: String
    ) : QmsMessage
}

fun QmsMessage.asRegular(): QmsMessage.Regular? {
    return this as? QmsMessage.Regular
}
