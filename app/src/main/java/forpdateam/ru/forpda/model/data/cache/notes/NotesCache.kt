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
        realm.where(NoteItemBd::class.java).findAll().sort("id", Sort.DESCENDING).map { NoteItem(it) }
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
            } ?: NoteItemBd(item)
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
                            currentItems[index] = NoteItem(newItem)
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
            realmTr.insertOrUpdate(NoteItemBd(item))
        }
        dataRelay.accept(getItems())
    }

    fun add(items: List<NoteItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(items.map { NoteItemBd(it) })
        }

        dataRelay.accept(getItems())
    }

    private fun getItemById(id: Long, realm: Realm) = realm.where(NoteItemBd::class.java)
            .equalTo("id", id)
            .findFirst()

}