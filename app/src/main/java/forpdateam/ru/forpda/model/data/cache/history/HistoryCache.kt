package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemDb
import forpdateam.ru.forpda.extensions.mapInnerList
import forpdateam.ru.forpda.model.data.db.HistoryDao
import kotlinx.coroutines.flow.Flow

class HistoryCache(
    private val historyDao: HistoryDao
) {

    fun observeItems(): Flow<List<HistoryItem>> {
        return historyDao.observeAll().mapInnerList { it.toDomain() }
    }

    suspend fun add(id: Int, url: String, title: String) {
        val newItem = HistoryItemDb(
            id = id,
            url = url,
            title = title,
            timestamp = System.currentTimeMillis(),
        )
        historyDao.upsert(newItem)
    }

    suspend fun remove(id: Int) {
        historyDao.deleteById(id)
    }

    suspend fun clear() {
        historyDao.deleteAll()
    }
}

fun HistoryItemDb.toDomain(): HistoryItem {
    return HistoryItem(
        id = id,
        url = url,
        title = title,
        timestamp = timestamp
    )
}

fun HistoryItem.toDb(): HistoryItemDb {
    return HistoryItemDb(
        id = id,
        url = url,
        title = title,
        timestamp = timestamp
    )
}