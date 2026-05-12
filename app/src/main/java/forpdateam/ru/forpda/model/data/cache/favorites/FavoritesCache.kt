package forpdateam.ru.forpda.model.data.cache.favorites

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.mapInnerList
import forpdateam.ru.forpda.model.data.db.FavoritesDao
import kotlinx.coroutines.flow.Flow

class FavoritesCache(
    private val favoritesDao: FavoritesDao,
    private val database: RoomDatabase
) {

    fun observeItems(): Flow<List<FavItem>> {
        return favoritesDao.observeAll().mapInnerList { it.toDomain() }
    }

    suspend fun getItems(): List<FavItem> {
        return favoritesDao.getAll().map { it.toDomain() }
    }

    suspend fun saveFavorites(items: List<FavItem>) {
        database.withTransaction {
            favoritesDao.deleteAll()
            favoritesDao.upsertAll(items.map { it.toDb() })
        }
    }

    suspend fun getItemByTopicId(topicId: Int): FavItem? {
        return favoritesDao.getByTopicId(topicId)?.toDomain()
    }

    suspend fun updateItem(item: FavItem) {
        favoritesDao.upsert(item.toDb())
    }

}

fun FavItemBd.toDomain(): FavItem {
    return FavItem(
        favId = favId,
        topicId = topicId,
        forumId = forumId,
        author = User.required(authorId, authorUserNick),
        lastUser = User.required(lastUserId, lastUserNick),
        curator = User.optional(curatorId, curatorNick),
        stParam = stParam,
        pages = pages,
        trackType = trackType,
        infoColor = infoColor,
        topicTitle = topicTitle,
        forumTitle = forumTitle,
        date = date,
        desc = desc,
        subType = subType,
        isPin = isPin,
        isForum = isForum,
        isNew = isNew,
        isPoll = isPoll,
        isClosed = isClosed
    )
}

fun FavItem.toDb(): FavItemBd {
    return FavItemBd(
        favId = favId,
        topicId = topicId,
        forumId = forumId,
        authorId = author.id,
        authorUserNick = author.nick,
        lastUserId = lastUser.id,
        lastUserNick = lastUser.nick,
        curatorId = curator?.id ?: 0,
        curatorNick = curator?.nick,
        stParam = stParam,
        pages = pages,
        trackType = trackType,
        infoColor = infoColor,
        topicTitle = topicTitle,
        forumTitle = forumTitle,
        date = date,
        desc = desc,
        subType = subType,
        isPin = isPin,
        isForum = isForum,
        isNew = isNew,
        isPoll = isPoll,
        isClosed = isClosed
    )
}