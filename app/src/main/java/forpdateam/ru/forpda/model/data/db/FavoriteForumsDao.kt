package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.favorites.FavoriteForumDb

@Dao
interface FavoriteForumsDao {

    @Upsert
    suspend fun upsert(item: FavoriteForumDb)

    @Upsert
    suspend fun upsertAll(items: List<FavoriteForumDb>)

    @Query("DELETE FROM favorite_forums")
    suspend fun deleteAll()

}