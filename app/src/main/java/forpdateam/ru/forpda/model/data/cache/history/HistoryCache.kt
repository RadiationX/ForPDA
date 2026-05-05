package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.query
import forpdateam.ru.forpda.common.realm.wrapper.queryEquals
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryCache(
    private val realm: RealmWrapper
) {

    private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())

    fun observeItems(): Flow<List<HistoryItem>> = realm
        .query<HistoryItemBd>()
        .sort("unixTime", Sort.DESCENDING)
        .flowMapAll { it.toDomain() }

    suspend fun getHistory(): List<HistoryItem> {
        return observeItems().first()
    }

    suspend fun add(id: Int, url: String, title: String) {
        realm.write {
            val unixTime = System.currentTimeMillis()
            val newItem = HistoryItemBd(
                id = id,
                url = url,
                date = dateFormat.format(Date(unixTime)),
                title = title,
                unixTime = unixTime,
            )
            upsert(newItem)
        }
    }

    suspend fun remove(id: Int) {
        realm.write {
            val toDelete = queryEquals<HistoryItemBd>("id", id).all()
            delete(toDelete)
        }
    }

    suspend fun clear() {
        realm.write {
            delete(HistoryItemBd::class)
        }
    }
}

fun HistoryItemBd.toDomain(): HistoryItem {
    return HistoryItem(id, url, title, unixTime, date)
}

fun HistoryItem.toDb(): HistoryItemBd {
    return HistoryItemBd(id, url, date, title, unixTime)
}