package forpdateam.ru.forpda.entity.remote

/**
 * Created by radiationx on 27.04.17.
 */

open class BaseForumPost(
    override val date: String,
    override val avatar: String,
    override val nick: String,
    override val groupColor: String,
    override val group: String,
    override val reputation: String,
    override val body: String?,
    override val isCurator: Boolean,
    override val isOnline: Boolean,
    override val canMinusRep: Boolean,
    override val canPlusRep: Boolean,
    override val canReport: Boolean,
    override val canEdit: Boolean,
    override val canDelete: Boolean,
    override val canQuote: Boolean,
    override val id: Int,
    override val topicId: Int,
    override val userId: Int,
) : IBaseForumPost
