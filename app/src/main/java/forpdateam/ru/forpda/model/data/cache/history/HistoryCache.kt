package forpdateam.ru.forpda.model.data.cache.history

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import io.reactivex.Observable
import io.realm.Realm
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*

class HistoryCache {

    private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())

    private val dataRelay = BehaviorRelay.create<List<HistoryItem>>()

    fun observeItems(): Observable<List<HistoryItem>> = dataRelay.hide()

    fun getHistory(): List<HistoryItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(HistoryItemBd::class.java).findAll().sort("unixTime", Sort.DESCENDING).map { HistoryItem(it) }
    }.also {
        if (!dataRelay.hasValue()) {
            dataRelay.accept(it)
        }
    }

    fun add(id: Int, url: String?, title: String?) = Realm.getDefaultInstance().use { realm ->
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
                item.date = dateFormat.format(Date(item.getUnixTime()))
                realmTr.insertOrUpdate(item)
            }
        }
        dataRelay.accept(getHistory())
    }

    fun remove(id: Int) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(HistoryItemBd::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        }
        if (dataRelay.hasValue()) {
            val currentItems = dataRelay.value!!.toMutableList()
            val index = currentItems.indexOfFirst { id == it.id }
            if (index == -1) {
                dataRelay.accept(getHistory())
            } else {
                currentItems.removeAt(index)
                dataRelay.accept(currentItems)
            }
        }
    }

    fun clear() = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(HistoryItemBd::class.java)
        }
        dataRelay.accept(emptyList())
    }
}