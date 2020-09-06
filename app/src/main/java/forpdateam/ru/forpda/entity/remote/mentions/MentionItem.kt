package forpdateam.ru.forpda.entity.remote.mentions

/**
 * Created by radiationx on 21.01.17.
 */

class MentionItem {
    var title: String? = null
    var desc: String? = null
    var link: String? = null
    var date: String? = null
    var nick: String? = null
    var state = STATE_READ
    var type = TYPE_TOPIC

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
