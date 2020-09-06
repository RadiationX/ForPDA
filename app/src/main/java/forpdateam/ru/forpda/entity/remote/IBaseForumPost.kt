package forpdateam.ru.forpda.entity.remote

/**
 * Created by radiationx on 27.04.17.
 */

interface IBaseForumPost {
    val topicId: Int
    val forumId: Int
    val id: Int
    val date: String?
    val number: Int
    val avatar: String?
    val nick: String?
    val groupColor: String?
    val group: String?
    val userId: Int
    val reputation: String?
    val body: String?
    val isCurator: Boolean
    val isOnline: Boolean
    val canMinusRep: Boolean
    val canPlusRep: Boolean
    val canReport: Boolean
    val canEdit: Boolean
    val canDelete: Boolean
    val canQuote: Boolean
}
