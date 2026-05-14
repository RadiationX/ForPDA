package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.qms.QmsContactDb
import kotlinx.coroutines.flow.Flow

@Dao
interface QmsContactsDao {

    @Query("SELECT * FROM qms_contacts")
    fun observeAll(): Flow<List<QmsContactDb>>

    @Query("SELECT * FROM qms_contacts WHERE id = :userId")
    fun observeByUserId(userId: Int): Flow<QmsContactDb?>

    @Query("SELECT * FROM qms_contacts")
    suspend fun getAll(): List<QmsContactDb>

    @Query("SELECT * FROM qms_contacts WHERE id = :userId")
    suspend fun getByUserId(userId: Int): QmsContactDb?

    @Upsert
    suspend fun upsert(item: QmsContactDb)

    @Upsert
    suspend fun upsertAll(items: List<QmsContactDb>)

    @Query("DELETE FROM qms_contacts")
    fun deleteAll()

}