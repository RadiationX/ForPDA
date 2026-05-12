package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd

@Dao
interface ForumsDao {

    @Query("SELECT * FROM forum_item_flat")
    suspend fun getAll(): List<ForumItemFlatBd>

    @Upsert
    suspend fun upsertAll(items: List<ForumItemFlatBd>)

    @Query("DELETE FROM forum_item_flat")
    suspend fun deleteAll()
}