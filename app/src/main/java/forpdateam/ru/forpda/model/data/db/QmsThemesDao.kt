package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.qms.QmsThemeDb
import kotlinx.coroutines.flow.Flow

@Dao
interface QmsThemesDao {

    @Query("SELECT * FROM qms_threads WHERE user_id = :userId")
    fun observeByUserId(userId: Int): Flow<List<QmsThemeDb>>

    @Query("SELECT * FROM qms_threads WHERE user_id = :userId")
    suspend fun getByUserId(userId: Int): List<QmsThemeDb>

    @Query("SELECT * FROM qms_threads")
    suspend fun getAll(): List<QmsThemeDb>

    @Upsert
    suspend fun upsertAll(items: List<QmsThemeDb>)

    @Query("DELETE FROM qms_threads WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Int)
}