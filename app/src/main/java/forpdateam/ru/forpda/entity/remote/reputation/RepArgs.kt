package forpdateam.ru.forpda.entity.remote.reputation

import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi

/**
 * Created by radiationx on 20.03.17.
 */

data class RepArgs(
    val userId: Int,
    val initialSt: Int,
    val mode: String,
    val sort: String
) {
    companion object {
        fun empty(): RepArgs = RepArgs(0, 0, ReputationApi.MODE_TO, ReputationApi.SORT_DESC)
    }
}
