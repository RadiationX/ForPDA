package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import kotlinx.coroutines.flow.Flow

@Dao
interface QmsThemesDao {

    @Query("SELECT * FROM qms_threads WHERE userId = :userId")
    fun observeByUserId(userId: Int): Flow<List<QmsThemeBd>>

    @Query("SELECT * FROM qms_threads WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): List<QmsThemeBd>

    @Query("SELECT * FROM qms_threads")
    suspend fun getAll(): List<QmsThemeBd>

    @Upsert
    suspend fun upsertAll(items: List<QmsThemeBd>)

    @Query("DELETE FROM qms_threads WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)
}