package forpdateam.ru.forpda.entity.remote.mentions

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 21.01.17.
 */

data class MentionsData(
    val items: List<MentionItem>,
    val pagination: Pagination
)
