package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.ForumUserBd

/**
 * Created by radiationx on 08.07.17.
 */

@Dao
interface ForumUsersDao {

    @Query("SELECT * FROM forum_users WHERE id = :id")
    suspend fun getById(id: Int): ForumUserBd?

    @Query("SELECT * FROM forum_users WHERE nick = :nick")
    suspend fun getByNick(nick: String): ForumUserBd?

    @Upsert
    suspend fun upsert(item: ForumUserBd)

    @Upsert
    suspend fun upsertAll(items: List<ForumUserBd>)

}
