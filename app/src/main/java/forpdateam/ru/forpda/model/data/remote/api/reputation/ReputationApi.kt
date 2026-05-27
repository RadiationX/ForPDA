package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import javax.inject.Inject

/**
 * Created by radiationx on 20.03.17.
 */

class ReputationApi @Inject constructor(
    private val webClient: WebClient,
    private val reputationParser: ReputationParser,
    private val linkTransformer: LinkTransformer
) {

    suspend fun getReputation(link: Link.Board.Reputation): RepData {
        val response = webClient.get(linkTransformer.build(link).toString())
        return reputationParser.parse(response.body)
    }

    suspend fun editReputation(postId: PostId?, userId: UserId, type: Boolean, message: String) {
        val type = if (type) "add" else "minus"
        webClient.request(ApiRequest.Forum.Reputation.Edit(postId, userId, type, message))
    }
}
