package forpdateam.ru.forpda.model.repository.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class HistoryRepository(
        private val schedulers: SchedulersProvider,
        private val historyCache: HistoryCache
) : BaseRepository(schedulers) {

    fun observeItems(): Observable<List<HistoryItem>> = historyCache
            .observeItems()
            .runInIoToUi()

    fun getHistory(): Single<List<HistoryItem>> = Single
            .fromCallable { historyCache.getHistory() }
            .runInIoToUi()

    fun remove(id: Int): Completable = Completable
            .fromRunnable { historyCache.remove(id) }
            .runInIoToUi()

    fun clear(): Completable = Completable
            .fromRunnable { historyCache.clear() }
            .runInIoToUi()

}
