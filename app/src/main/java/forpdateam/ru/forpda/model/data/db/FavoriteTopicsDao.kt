package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.favorites.FavoriteTopicDb

@Dao
interface FavoriteTopicsDao {

    @Query("SELECT * FROM favorite_topics WHERE topic_id = :topicId")
    suspend fun getByTopicId(topicId: Int): FavoriteTopicDb?

    @Upsert
    suspend fun upsert(item: FavoriteTopicDb)

    @Upsert
    suspend fun upsertAll(items: List<FavoriteTopicDb>)

    @Query("DELETE FROM favorite_topics")
    suspend fun deleteAll()

}