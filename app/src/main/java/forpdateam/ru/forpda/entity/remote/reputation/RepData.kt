package forpdateam.ru.forpda.entity.remote.reputation

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 20.03.17.
 */

class RepData(
    val id: Int,
    val positive: Int,
    val negative: Int,
    val nick: String?,
    val pagination: Pagination,
    val items: List<RepItem>
)
