package forpdateam.ru.forpda.model.data.cache.forum

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.model.data.db.ForumsDao

class ForumCache(
    private val forumsDao: ForumsDao,
    private val database: RoomDatabase
) {

    suspend fun getItems(): List<ForumItemFlat> {
        return forumsDao.getAll().map { it.toDomain() }
    }

    suspend fun saveItems(items: List<ForumItemFlat>) {
        database.withTransaction {
            forumsDao.deleteAll()
            forumsDao.upsertAll(items.map { it.toDb() })
        }
    }
}

fun ForumItemFlatBd.toDomain(): ForumItemFlat {
    return ForumItemFlat(id, parentId, level, title)
}

fun ForumItemFlat.toDb(): ForumItemFlatBd {
    return ForumItemFlatBd(id, parentId, level, title)
}