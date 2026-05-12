package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY unixTime DESC")
    fun observeAll(): Flow<List<HistoryItemBd>>

    @Upsert
    suspend fun upsert(item: HistoryItemBd)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
