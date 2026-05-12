package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import kotlinx.coroutines.flow.Flow

@Dao
interface QmsContactsDao {

    @Query("SELECT * FROM qms_contacts")
    fun observeAll(): Flow<List<QmsContactBd>>

    @Query("SELECT * FROM qms_contacts WHERE id = :userId")
    fun observeByUserId(userId: Int): Flow<QmsContactBd?>

    @Query("SELECT * FROM qms_contacts")
    suspend fun getAll(): List<QmsContactBd>

    @Query("SELECT * FROM qms_contacts WHERE id = :userId")
    suspend fun getByUserId(userId: Int): QmsContactBd?

    @Upsert
    suspend fun upsert(item: QmsContactBd)

    @Upsert
    suspend fun upsertAll(items: List<QmsContactBd>)

    @Query("DELETE FROM qms_contacts")
    fun deleteAll()

}