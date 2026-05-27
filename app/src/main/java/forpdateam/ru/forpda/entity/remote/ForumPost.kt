package forpdateam.ru.forpda.entity.remote

import forpdateam.ru.forpda.entity.remote.others.user.ForumPostUser
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId

/**
 * Created by radiationx on 27.04.17.
 */

data class ForumPost(
    val date: String,
    val groupColor: String,
    val group: String,
    val reputation: String,
    val body: String,
    val isCurator: Boolean,
    val isOnline: Boolean,
    val canMinusRep: Boolean,
    val canPlusRep: Boolean,
    val canReport: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canQuote: Boolean,
    val id: PostId,
    val topicId: TopicId,
    val user: ForumPostUser
) 
