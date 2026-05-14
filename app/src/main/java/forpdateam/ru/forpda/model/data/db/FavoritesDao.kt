package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import forpdateam.ru.forpda.entity.db.favorites.FavoriteDb
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Transaction
    @Query("SELECT * FROM favorite_ids")
    fun observeAll(): Flow<List<FavoriteDb>>

    @Transaction
    @Query("SELECT * FROM favorite_ids")
    suspend fun getAll(): List<FavoriteDb>

}