package forpdateam.ru.forpda.entity.remote.mentions

/**
 * Created by radiationx on 21.01.17.
 */

data class MentionItem(
    val title: String,
    val desc: String,
    val link: String,
    val date: String,
    val nick: String,
    val state: Int,
    val type: Int
) {

    val isRead: Boolean
        get() = state == STATE_READ

    val isTopic: Boolean
        get() = type == TYPE_TOPIC

    companion object {
        val STATE_UNREAD = 0
        val STATE_READ = 1
        val TYPE_TOPIC = 0
        val TYPE_NEWS = 1
    }
}
