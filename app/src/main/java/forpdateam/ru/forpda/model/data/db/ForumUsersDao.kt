package forpdateam.ru.forpda.model.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import forpdateam.ru.forpda.entity.db.ForumUserDb
import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 08.07.17.
 */

@Dao
interface ForumUsersDao {

    @Query("SELECT * FROM forum_users WHERE id = :id")
    fun observeById(id: Int): Flow<ForumUserDb?>

    @Query("SELECT * FROM forum_users WHERE id = :id")
    suspend fun getById(id: Int): ForumUserDb?

    @Query("SELECT * FROM forum_users WHERE nick = :nick")
    suspend fun getByNick(nick: String): ForumUserDb?

    @Upsert
    suspend fun upsert(item: ForumUserDb)

    @Upsert
    suspend fun upsertAll(items: List<ForumUserDb>)

}
