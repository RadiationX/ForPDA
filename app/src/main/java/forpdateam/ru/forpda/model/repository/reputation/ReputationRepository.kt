package forpdateam.ru.forpda.model.repository.reputation

import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 03.01.18.
 */

class ReputationRepository(
        private val schedulers: SchedulersProvider,
        private val reputationApi: ReputationApi
) : BaseRepository(schedulers) {

    fun loadReputation(userId: Int, mode: String, sort: String, st: Int): Single<RepData> = Single
            .fromCallable { reputationApi.getReputation(userId, mode, sort, st) }
            .runInIoToUi()

    fun changeReputation(postId: Int, userId: Int, type: Boolean, message: String): Single<Boolean> = Single
            .fromCallable { reputationApi.editReputation(postId, userId, type, message) }
            .runInIoToUi()

}
