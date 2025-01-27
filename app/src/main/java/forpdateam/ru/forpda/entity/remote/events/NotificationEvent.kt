package forpdateam.ru.forpda.entity.remote.events

/**
 * Created by radiationx on 29.07.17.
 */

data class NotificationEvent(
    val type: Type,
    val source: Source,

    val messageId: Int,

    val sourceId: Int,
    val userId: Int,

    val timeStamp: Long,
    val lastTimeStamp: Long,

    val msgCount: Int,
    val isImportant: Boolean,

    val sourceTitle: String,
    val userNick: String,

    val sourceEventText: String?
) {


    /*
    * short
    * */

    val isNew: Boolean
        get() = isNew(type)

    val isRead: Boolean
        get() = isRead(type)

    val isMention: Boolean
        get() = isMention(type)


    enum class Type(val value: Int) {
        NEW(2),
        READ(4),
        MENTION(8),
        HAT_EDITED(16)
    }

    enum class Source(val value: Int) {
        THEME(32),
        SITE(64),
        QMS(128)
    }

    fun fromTheme(): Boolean {
        return fromTheme(source)
    }

    fun fromSite(): Boolean {
        return fromSite(source)
    }

    fun fromQms(): Boolean {
        return fromQms(source)
    }

    @JvmOverloads
    fun notifyId(type: Type? = this.type): Int {
        return sourceId / 4 + type!!.value + type.value
    }

    companion object {
        const val SRC_EVENT_NEW = 1
        const val SRC_EVENT_READ = 2
        const val SRC_EVENT_MENTION = 3
        const val SRC_EVENT_HAT_EDITED = 4
        const val SRC_TYPE_SITE = "s"
        const val SRC_TYPE_THEME = "t"
        const val SRC_TYPE_QMS = "q"


        fun isNew(type: Type?): Boolean {
            return type != null && type == Type.NEW
        }

        fun isRead(type: Type?): Boolean {
            return type != null && type == Type.READ
        }

        fun isMention(type: Type?): Boolean {
            return type != null && type == Type.MENTION
        }

        fun fromTheme(source: Source?): Boolean {
            return source != null && source == Source.THEME
        }

        fun fromSite(source: Source?): Boolean {
            return source != null && source == Source.SITE
        }

        fun fromQms(source: Source?): Boolean {
            return source != null && source == Source.QMS
        }
    }
}
