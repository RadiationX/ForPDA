package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.notes.NoteItemDb
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun observeALl(): Flow<List<NoteItemDb>>

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAll(): List<NoteItemDb>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Upsert
    suspend fun upsert(item: NoteItemDb)

    @Upsert
    suspend fun upsertAll(items: List<NoteItemDb>)
}