package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun observeALl(): Flow<List<NoteItemBd>>

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAll(): List<NoteItemBd>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Upsert
    suspend fun upsert(item: NoteItemBd)

    @Upsert
    suspend fun upsertAll(items: List<NoteItemBd>)
}