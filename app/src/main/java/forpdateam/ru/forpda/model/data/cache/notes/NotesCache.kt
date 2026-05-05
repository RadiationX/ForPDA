package forpdateam.ru.forpda.model.data.cache.notes

import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class NotesCache {

    private val dataFlow = MutableStateFlow<List<NoteItem>?>(null)

    fun observeItems(): Flow<List<NoteItem>> = dataFlow.filterNotNull()

    suspend fun getItems(): List<NoteItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(NoteItemBd::class.java).findAll().sort("id", Sort.DESCENDING)
            .map { it.toDomain() }
    }.also {
        if (dataFlow.value == null) {
            dataFlow.value = it
        }
    }

    suspend fun update(item: NoteItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            val itemBd = getItemById(item.id, realmTr)?.apply {
                title = item.title
                link = item.link
                content = item.content
            } ?: item.toDb()
            realmTr.insertOrUpdate(itemBd)
        }
        if (dataFlow.value != null) {
            getItemById(item.id, realm)
                ?.also { newItem ->
                    val currentItems = dataFlow.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.id == it.id }
                    if (index == -1) {
                        dataFlow.value = getItems()
                    } else {
                        currentItems[index] = newItem.toDomain()
                        dataFlow.value = currentItems
                    }
                }
        }
    }

    suspend fun delete(id: Long) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(NoteItemBd::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        }
        if (dataFlow.value != null) {
            val currentItems = dataFlow.value!!.toMutableList()
            val index = currentItems.indexOfFirst { id == it.id }
            if (index == -1) {
                dataFlow.value = getItems()
            } else {
                currentItems.removeAt(index)
                dataFlow.value = currentItems
            }
        }
    }

    suspend fun add(item: NoteItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(item.toDb())
        }
        dataFlow.value = getItems()
    }

    suspend fun add(items: List<NoteItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.insertOrUpdate(items.map { it.toDb() })
        }
        dataFlow.value = getItems()
    }

    private suspend fun getItemById(id: Long, realm: Realm) = realm.where(NoteItemBd::class.java)
        .equalTo("id", id)
        .findFirst()

}

fun NoteItemBd.toDomain(): NoteItem {
    return NoteItem(id, title, link, content)
}

fun NoteItem.toDb(): NoteItemBd {
    return NoteItemBd(id, title, link, content)
}