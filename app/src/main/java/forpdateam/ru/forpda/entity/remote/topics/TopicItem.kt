package forpdateam.ru.forpda.entity.remote.topics

/**
 * Created by radiationx on 01.03.17.
 */
class TopicItem {
    var isPinned: Boolean = false
    var isAnnounce: Boolean = false
    var isForum: Boolean = false
    @JvmField
    var isNew: Boolean = false
    var isPoll: Boolean = false
    var isClosed: Boolean = false
    var id: Int = 0
    var authorId: Int = 0
    var lastUserId: Int = 0
    var curatorId: Int = 0
    var title: String? = null
    var desc: String? = null
    var authorNick: String? = null
    var lastUserNick: String? = null
    var date: String? = null
    var curatorNick: String? = null
    var announceUrl: String? = null
}
