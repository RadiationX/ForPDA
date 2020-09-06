package forpdateam.ru.forpda.model.repository.topics

import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.topcis.TopicsApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 03.01.18.
 */

class TopicsRepository(
        private val schedulers: SchedulersProvider,
        private val topicsApi: TopicsApi
) : BaseRepository(schedulers) {

    fun getTopics(id: Int, st: Int): Single<TopicsData> = Single
            .fromCallable { topicsApi.getTopics(id, st) }
            .runInIoToUi()

}
