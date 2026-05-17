package forpdateam.ru.forpda.model.repository.reputation

import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import javax.inject.Inject

/**
 * Created by radiationx on 03.01.18.
 */

class ReputationRepository @Inject constructor(
    private val reputationApi: ReputationApi
) {

    suspend fun loadReputation(userId: Int, mode: String, sort: String, st: Int): RepData {
        return reputationApi.getReputation(userId, mode, sort, st)
    }

    suspend fun changeReputation(
        postId: Int,
        userId: Int,
        type: Boolean,
        message: String
    ) {
        reputationApi.editReputation(postId, userId, type, message)
    }

}
