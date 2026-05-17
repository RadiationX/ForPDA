package forpdateam.ru.forpda.model.repository.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class HistoryRepository @Inject constructor(
    private val historyCache: HistoryCache
) {

    fun observeItems(): Flow<List<HistoryItem>> {
        return historyCache.observeItems()
    }

    suspend fun remove(id: Int) {
        historyCache.remove(id)
    }

    suspend fun clear() {
        historyCache.clear()
    }

}
