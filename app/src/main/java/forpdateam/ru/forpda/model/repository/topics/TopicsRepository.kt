package forpdateam.ru.forpda.model.repository.topics

import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsApi
import javax.inject.Inject

/**
 * Created by radiationx on 03.01.18.
 */

class TopicsRepository @Inject constructor(
    private val topicsApi: TopicsApi
) {

    suspend fun getTopics(id: Int, st: Int): TopicsData {
        return topicsApi.getTopics(id, st)
    }

}
