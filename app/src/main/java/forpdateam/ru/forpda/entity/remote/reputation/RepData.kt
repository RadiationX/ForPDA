package forpdateam.ru.forpda.entity.remote.reputation

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import ru.radiationx.coretypes.UserId

/**
 * Created by radiationx on 20.03.17.
 */

data class RepData(
    val id: UserId,
    val positive: Int,
    val negative: Int,
    val nick: String,
    val pagination: Pagination,
    val items: List<RepItem>
)
