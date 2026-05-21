package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 20.03.17.
 */

class ReputationApi @Inject constructor(
    private val webClient: WebClient,
    private val reputationParser: ReputationParser
) {

    suspend fun getReputation(userId: Int, mode: String, sort: String, st: Int): RepData {
        val response = webClient.request(ApiRequest.Forum.Reputation.GetPage(userId, mode, sort, st))
        return reputationParser.parse(response.body)
    }

    suspend fun editReputation(postId: Int, userId: Int, type: Boolean, message: String) {
        val type = if (type) "add" else "minus"
        webClient.request(ApiRequest.Forum.Reputation.Edit(postId, userId, type, message))
    }
}
