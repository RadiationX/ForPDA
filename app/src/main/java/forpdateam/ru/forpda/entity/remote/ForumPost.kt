package forpdateam.ru.forpda.entity.remote

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser

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
    val id: Int,
    val topicId: Int,
    val user: ForumUser
) 
