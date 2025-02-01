package forpdateam.ru.forpda.entity.remote

/**
 * Created by radiationx on 27.04.17.
 */

class BaseForumPost(
    val date: String,
    val avatar: String?,
    val nick: String,
    val groupColor: String,
    val group: String,
    val reputation: String,
    val body: String?,
    val isCurator: Boolean,
    val isOnline: Boolean,
    val canMinusRep: Boolean,
    val canPlusRep: Boolean,
    val canReport: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canQuote: Boolean,
    val id: Int,
    val topicId: Int,
    val userId: Int,
) 
