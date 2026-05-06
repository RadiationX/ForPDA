package forpdateam.ru.forpda.model.data.cache.notes

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.query
import forpdateam.ru.forpda.common.realm.wrapper.queryEquals
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import io.github.xilinjia.krdb.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotesCache(
    private val realm: RealmWrapper
) {

    fun observeItems(): Flow<List<NoteItem>> = realm
        .query<NoteItemBd>()
        .sort("id", Sort.DESCENDING)
        .flowMapAll { it.toDomain() }

    suspend fun getItems(): List<NoteItem> {
        return observeItems().first()
    }

    suspend fun delete(id: Long) {
        realm.write {
            val toDelete = queryEquals<NoteItemBd>("id", id).all()
            delete(toDelete)
        }
    }

    suspend fun add(item: NoteItem) {
        realm.write {
            upsert(item.toDb())
        }
    }

    suspend fun add(items: List<NoteItem>) {
        realm.write {
            upsertAll(items.map { it.toDb() })
        }
    }
}

fun NoteItemBd.toDomain(): NoteItem {
    return NoteItem(id, title, link, content)
}

fun NoteItem.toDb(): NoteItemBd {
    return NoteItemBd(id, title, link, content)
}