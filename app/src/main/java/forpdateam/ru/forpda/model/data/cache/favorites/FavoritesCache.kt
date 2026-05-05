package forpdateam.ru.forpda.model.data.cache.favorites

import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import io.realm.Realm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FavoritesCache {

    private val dataFlow = MutableStateFlow<List<FavItem>?>(null)

    fun observeItems(): Flow<List<FavItem>> = dataFlow.filterNotNull()

    suspend fun getItems(): List<FavItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).findAll().map { it.toDomain() }
    }.also {
        if (dataFlow.value == null) {
            dataFlow.value = it
        }
    }

    suspend fun saveFavorites(items: List<FavItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(FavItemBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { it.toDb() })
        }
        dataFlow.value = getItems()
    }

    suspend fun getItemByFavId(favId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("favId", favId).findFirst()?.toDomain()
    }

    suspend fun getItemByTopicId(topicId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("topicId", topicId).findFirst()?.toDomain()
    }

    suspend fun updateItem(item: FavItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(FavItemBd::class.java).equalTo("favId", item.favId).findFirst()?.let {
                realmTr.copyToRealmOrUpdate(item.toDb())
            }
        }
        if (dataFlow.value != null) {
            realm.where(FavItemBd::class.java)
                .equalTo("favId", item.favId)
                .findFirst()
                ?.also { newItem ->
                    val currentItems = dataFlow.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.favId == it.favId }
                    if (index == -1) {
                        dataFlow.value = getItems()
                    } else {
                        currentItems[index] = newItem.toDomain()
                        dataFlow.value = currentItems
                    }
                }
        }
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