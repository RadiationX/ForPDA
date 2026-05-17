package forpdateam.ru.forpda.model.data.remote.api.topcis

import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.IWebClient
import javax.inject.Inject

/**
 * Created by radiationx on 01.03.17.
 */

class TopicsApi @Inject constructor(
    private val webClient: IWebClient,
    private val topicsParser: TopicsParser
) {

    suspend fun getTopics(id: Int, st: Int): TopicsData {
        val response = webClient.get("https://4pda.to/forum/index.php?showforum=$id&st=$st")
        return topicsParser.parse(response.body, id)
    }
}
