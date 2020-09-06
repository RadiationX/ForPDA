package forpdateam.ru.forpda.entity.remote.reputation

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi

/**
 * Created by radiationx on 20.03.17.
 */

class RepData {
    var id = 0
    var positive = 0
    var negative = 0
    var nick: String? = null
    var mode = ReputationApi.MODE_TO
    var sort = ReputationApi.SORT_DESC
    var pagination = Pagination()
    val items = mutableListOf<RepItem>()
}
