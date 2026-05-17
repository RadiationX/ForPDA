package forpdateam.ru.forpda.model.data.cache.notes

import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemDb
import forpdateam.ru.forpda.extensions.mapInnerList
import forpdateam.ru.forpda.model.data.db.NotesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotesCache @Inject constructor(
    private val notesDao: NotesDao
) {

    fun observeItems(): Flow<List<NoteItem>> {
        return notesDao.observeALl().mapInnerList { it.toDomain() }
    }

    suspend fun getItems(): List<NoteItem> {
        return notesDao.getAll().map { it.toDomain() }
    }

    suspend fun delete(id: Long) {
        notesDao.deleteById(id)
    }

    suspend fun add(item: NoteItem) {
        notesDao.upsert(item.toDb())
    }

    suspend fun add(items: List<NoteItem>) {
        notesDao.upsertAll(items.map { it.toDb() })
    }
}

fun NoteItemDb.toDomain(): NoteItem {
    return NoteItem(id, title, link, content)
}

fun NoteItem.toDb(): NoteItemDb {
    return NoteItemDb(id, title, link, content)
}