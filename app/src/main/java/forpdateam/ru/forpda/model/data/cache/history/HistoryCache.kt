package forpdateam.ru.forpda.model.data.cache.history

import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryCache {

    private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())

    private val dataFlow = MutableStateFlow<List<HistoryItem>?>(null)

    fun observeItems(): Flow<List<HistoryItem>> = dataFlow.filterNotNull()

    suspend fun getHistory(): List<HistoryItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(HistoryItemBd::class.java).findAll().sort("unixTime", Sort.DESCENDING)
            .map { it.toDomain() }
    }.also {
        if (dataFlow.value == null) {
            dataFlow.value = it
        }
    }

    suspend fun add(id: Int, url: String?, title: String?) =
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realmTr ->
                val item = realmTr.where(HistoryItemBd::class.java).equalTo("id", id).findFirst()
                if (item == null) {
                    realmTr.insert(HistoryItemBd().apply {
                        this.title = title
                        this.id = id
                        this.url = url
                        unixTime = System.currentTimeMillis()
                        date = dateFormat.format(Date(unixTime))
                    })
                } else {
                    item.url = url
                    item.unixTime = System.currentTimeMillis()
                    item.date = dateFormat.format(Date(item.unixTime))
                    realmTr.insertOrUpdate(item)
                }
            }
            dataFlow.value = getHistory()
        }

    suspend fun remove(id: Int) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(HistoryItemBd::class.java).equalTo("id", id).findAll()
                .deleteAllFromRealm()
        }
        if (dataFlow.value != null) {
            val currentItems = dataFlow.value!!.toMutableList()
            val index = currentItems.indexOfFirst { id == it.id }
            if (index == -1) {
                dataFlow.value = getHistory()
            } else {
                currentItems.removeAt(index)
                dataFlow.value = currentItems
            }
        }
    }

    suspend fun clear() = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(HistoryItemBd::class.java)
        }
        dataFlow.value = emptyList()
    }
}

fun HistoryItemBd.toDomain(): HistoryItem {
    return HistoryItem(id, url, title, unixTime, date)
}

fun HistoryItem.toDb(): HistoryItemBd {
    return HistoryItemBd(id, url, date, title, unixTime)
}