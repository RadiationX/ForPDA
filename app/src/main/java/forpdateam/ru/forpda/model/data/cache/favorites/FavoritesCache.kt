package forpdateam.ru.forpda.model.data.cache.favorites

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import forpdateam.ru.forpda.entity.db.UserDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteForumDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteIdDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteTopicDb
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.db.FavoriteForumsDao
import forpdateam.ru.forpda.model.data.db.FavoriteIdsDao
import forpdateam.ru.forpda.model.data.db.FavoriteTopicsDao
import forpdateam.ru.forpda.model.data.db.FavoritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class FavoritesCache @Inject constructor(
    private val favoritesDao: FavoritesDao,
    private val favoriteIdsDao: FavoriteIdsDao,
    private val favoriteTopicsDao: FavoriteTopicsDao,
    private val favoriteForumsDao: FavoriteForumsDao,
    private val database: RoomDatabase
) {

    fun observeItems(): Flow<List<Favorite>> {
        return favoritesDao.observeAll().map { items ->
            items.mapNotNull { it.toDomain() }
        }
    }

    suspend fun getItems(): List<Favorite> {
        return favoritesDao.getAll().mapNotNull { it.toDomain() }
    }

    suspend fun saveFavorites(items: List<Favorite>) {
        database.withTransaction {
            favoriteIdsDao.deleteAll()
            favoriteTopicsDao.deleteAll()
            favoriteForumsDao.deleteAll()

            favoriteTopicsDao.upsertAll(items.filterIsInstance<Favorite.Topic>().map { it.toTopicDb() })
            favoriteForumsDao.upsertAll(items.filterIsInstance<Favorite.Forum>().map { it.toForumDb() })
            favoriteIdsDao.upsertAll(items.map { it.toIdDb() })
        }
    }

    suspend fun getItemByTopicId(topicId: TopicId): Favorite.Topic? {
        return favoriteTopicsDao.getByTopicId(topicId.id)?.toDomain()
    }

    suspend fun updateItem(item: Favorite) {
        database.withTransaction {
            when (item) {
                is Favorite.Topic -> favoriteTopicsDao.upsert(item.toTopicDb())
                is Favorite.Forum -> favoriteForumsDao.upsert(item.toForumDb())
            }
            favoriteIdsDao.upsert(item.toIdDb())
        }
    }

}

fun UserDb.toDomain(): User {
    return User(id = UserId(id), nick = nick)
}

fun User.toDb(): UserDb {
    return UserDb(id = id.id, nick = nick)
}

fun FavoriteDb.toDomain(): Favorite? {
    return when {
        topic != null -> topic.toDomain()
        forum != null -> forum.toDomain()
        else -> null
    }
}

fun FavoriteTopicDb.toDomain(): Favorite.Topic {
    return Favorite.Topic(
        id = FavoriteId(favId),
        topicId = TopicId(topicId),
        title = title,
        trackType = trackType,
        isPin = isPin,
        isNew = isNew,
        isPoll = isPoll,
        isClosed = isClosed,
        stParam = stParam,
        desc = desc,
        forumId = ForumId(forumId),
        forumTitle = forumTitle,
        author = author.toDomain(),
        lastUser = lastUser.toDomain(),
        date = date,
        curator = curator?.toDomain()
    )
}

fun FavoriteForumDb.toDomain(): Favorite.Forum {
    return Favorite.Forum(
        id = FavoriteId(favId),
        forumId = ForumId(forumId),
        title = title,
        trackType = trackType,
        isPin = isPin,
        isNew = isNew,
        date = date,
        lastUser = lastUser?.toDomain()
    )
}

fun Favorite.toIdDb(): FavoriteIdDb {
    return FavoriteIdDb(id.id)
}

fun Favorite.Topic.toTopicDb(): FavoriteTopicDb {
    return FavoriteTopicDb(
        favId = id.id,
        topicId = topicId.id,
        title = title,
        trackType = trackType,
        isPin = isPin,
        isNew = isNew,
        isPoll = isPoll,
        isClosed = isClosed,
        stParam = stParam,
        desc = desc,
        forumId = forumId.id,
        forumTitle = forumTitle,
        author = author.toDb(),
        lastUser = lastUser.toDb(),
        date = date,
        curator = curator?.toDb()
    )
}

fun Favorite.Forum.toForumDb(): FavoriteForumDb {
    return FavoriteForumDb(
        favId = id.id,
        forumId = forumId.id,
        title = title,
        trackType = trackType,
        isPin = isPin,
        isNew = isNew,
        date = date,
        lastUser = lastUser?.toDb()
    )
}
