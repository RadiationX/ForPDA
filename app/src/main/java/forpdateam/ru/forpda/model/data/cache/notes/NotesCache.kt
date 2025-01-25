package forpdateam.ru.forpda.model.data.cache.notes

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import io.reactivex.Observable
import io.realm.Realm
import io.realm.Sort

class NotesCache {

    private val dataRelay = BehaviorRelay.create<List<NoteItem>>()

    fun observeItems(): Observable<List<NoteItem>> = dataRelay.hide()

    fun getItems(): List<NoteItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(NoteItemBd::class.java).findAll().sort("id", Sort.DESCENDING)
            .map { it.toDomain() }
    }.also {
        if (!dataRelay.hasValue()) {
            dataRelay.accept(it)
        }
    }

    fun update(item: NoteItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            val itemBd = getItemById(item.id, realmTr)?.apply {
                title = item.title
                link = item.link
                content = item.content
            } ?: item.toDb()
            realmTr.insertOrUpdate(itemBd)
        }
        if (dataRelay.hasValue()) {
            getItemById(item.id, realm)
                ?.also { newItem ->
                    val currentItems = dataRelay.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.id == it.id }
                    if (index == -1) {
                        dataRelay.accept(getItems())
                    } else {
                        currentItems[index] = newItem.toDomain()
                        dataRelay.accept(currentItems)
                    }
                }
        }
    }

    fun delete(id: Long) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(NoteItemBd::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        }
        if (dataRelay.hasValue()) {
            val currentItems = dataRelay.value!!.toMutableList()
            val index = currentItems.indexOfFirst { id == it.id }
            if (index == -1) {
                dataRelay.accept(getItems())
            } else {
                currentItems.removeAt(index)
                dataRelay.accept(currentItems)
            }
        }
    }

    fun add(item: NoteItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(item.toDb())
        }
        dataRelay.accept(getItems())
    }

    fun add(items: List<NoteItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(items.map { it.toDb() })
        }

        dataRelay.accept(getItems())
    }

    private fun getItemById(id: Long, realm: Realm) = realm.where(NoteItemBd::class.java)
        .equalTo("id", id)
        .findFirst()

}

fun NoteItemBd.toDomain(): NoteItem {
    return NoteItem(id, title, link, content)
}

fun NoteItem.toDb(): NoteItemBd {
    return NoteItemBd(id, title, link, content)
}