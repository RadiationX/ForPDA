package forpdateam.ru.forpda.model.data.cache.favorites

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.query
import forpdateam.ru.forpda.common.realm.wrapper.queryEquals
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FavoritesCache(
    private val realm: RealmWrapper
) {

    fun observeItems(): Flow<List<FavItem>> = realm
        .query<FavItemBd>()
        .flowMapAll { it.toDomain() }

    suspend fun getItems(): List<FavItem> {
        return observeItems().first()
    }

    suspend fun saveFavorites(items: List<FavItem>) {
        realm.write {
            delete(FavItemBd::class)
            upsertAll(items.map { it.toDb() })
        }
    }

    suspend fun getItemByTopicId(topicId: Int): FavItem? {
        return realm
            .queryEquals<FavItemBd>("topicId", topicId)
            .mapFirst { it.toDomain() }
    }

    suspend fun updateItem(item: FavItem) = realm.write {
        upsert(item.toDb())
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