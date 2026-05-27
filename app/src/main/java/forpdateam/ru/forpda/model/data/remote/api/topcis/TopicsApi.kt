package forpdateam.ru.forpda.model.data.remote.api.topcis

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import javax.inject.Inject

/**
 * Created by radiationx on 01.03.17.
 */

class TopicsApi @Inject constructor(
    private val webClient: WebClient,
    private val topicsParser: TopicsParser
) {

    suspend fun getTopics(forumId: ForumId, offset: PageOffset): TopicsData {
        val response = webClient.request(ApiRequest.Forum.Forums.GetTopics(forumId, offset))
        return topicsParser.parse(response.body, forumId)
    }
}
