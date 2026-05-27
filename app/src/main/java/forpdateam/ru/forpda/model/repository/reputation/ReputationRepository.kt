package forpdateam.ru.forpda.model.repository.reputation

import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Link
import javax.inject.Inject

/**
 * Created by radiationx on 03.01.18.
 */

class ReputationRepository @Inject constructor(
    private val reputationApi: ReputationApi
) {

    suspend fun loadReputation(link: Link.Board.Reputation): RepData {
        return reputationApi.getReputation(link)
    }

    suspend fun changeReputation(postId: PostId?, userId: UserId, type: Boolean, message: String) {
        reputationApi.editReputation(postId, userId, type, message)
    }

}
