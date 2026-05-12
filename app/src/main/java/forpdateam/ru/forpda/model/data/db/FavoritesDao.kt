package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorites")
    fun observeAll(): Flow<List<FavItemBd>>

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavItemBd>

    @Query("SELECT * FROM favorites WHERE topicId = :topicId")
    suspend fun getByTopicId(topicId: Int): FavItemBd?

    @Upsert
    suspend fun upsert(item: FavItemBd)

    @Upsert
    suspend fun upsertAll(items: List<FavItemBd>)

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()

}