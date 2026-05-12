package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import forpdateam.ru.forpda.extensions.mapInnerList
import forpdateam.ru.forpda.model.data.db.HistoryDao
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryCache(
    private val historyDao: HistoryDao
) {

    private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())

    fun observeItems(): Flow<List<HistoryItem>> {
        return historyDao.observeAll().mapInnerList { it.toDomain() }
    }

    suspend fun add(id: Int, url: String, title: String) {
        val unixTime = System.currentTimeMillis()
        val newItem = HistoryItemBd(
            id = id,
            url = url,
            date = dateFormat.format(Date(unixTime)),
            title = title,
            unixTime = unixTime,
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

fun HistoryItemBd.toDomain(): HistoryItem {
    return HistoryItem(id, url, title, unixTime, date)
}

fun HistoryItem.toDb(): HistoryItemBd {
    return HistoryItemBd(id, url, date, title, unixTime)
}