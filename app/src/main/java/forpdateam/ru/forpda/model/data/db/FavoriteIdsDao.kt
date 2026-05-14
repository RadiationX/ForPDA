package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.favorites.FavoriteIdDb

@Dao
interface FavoriteIdsDao {

    @Upsert
    suspend fun upsert(item: FavoriteIdDb)

    @Upsert
    suspend fun upsertAll(items: List<FavoriteIdDb>)

    @Query("DELETE FROM favorite_ids")
    suspend fun deleteAll()

}