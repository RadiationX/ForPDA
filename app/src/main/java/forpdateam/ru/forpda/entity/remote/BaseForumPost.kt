package forpdateam.ru.forpda.entity.remote

/**
 * Created by radiationx on 27.04.17.
 */

open class BaseForumPost : IBaseForumPost {
    override var date: String? = null
    override var avatar: String? = null
    override var nick: String? = null
    override var groupColor: String? = "black"
        set(groupColor) {
            if (groupColor == null)
                return
            field = groupColor
        }
    override var group: String? = null
    override var reputation: String? = null
    override var body: String? = null
    override var isCurator: Boolean = false
    override var isOnline: Boolean = false
    override var canMinusRep: Boolean = false
    override var canPlusRep: Boolean = false
    override var canReport: Boolean = false
    override var canEdit: Boolean = false
    override var canDelete: Boolean = false
    override var canQuote: Boolean = false
    override var id = 0
    override var topicId = 0
    override var forumId = 0
    override var number = 0
    override var userId = 0
}
