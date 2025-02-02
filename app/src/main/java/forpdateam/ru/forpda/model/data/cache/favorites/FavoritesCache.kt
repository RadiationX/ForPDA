package forpdateam.ru.forpda.model.data.cache.favorites

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import io.reactivex.Observable
import io.realm.Realm

class FavoritesCache {

    private val dataRelay = BehaviorRelay.create<List<FavItem>>()

    fun observeItems(): Observable<List<FavItem>> = dataRelay.hide()

    fun getItems(): List<FavItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).findAll().map { it.toDomain() }
    }.also {
        if (!dataRelay.hasValue()) {
            dataRelay.accept(it)
        }
    }

    fun saveFavorites(items: List<FavItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(FavItemBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { it.toDb() })
        }
        dataRelay.accept(getItems())
    }

    fun getItemByFavId(favId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("favId", favId).findFirst()?.toDomain()
    }

    fun getItemByTopicId(topicId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("topicId", topicId).findFirst()?.toDomain()
    }

    fun updateItem(item: FavItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(FavItemBd::class.java).equalTo("favId", item.favId).findFirst()?.let {
                realmTr.copyToRealmOrUpdate(item.toDb())
            }
        }
        if (dataRelay.hasValue()) {
            realm.where(FavItemBd::class.java)
                .equalTo("favId", item.favId)
                .findFirst()
                ?.also { newItem ->
                    val currentItems = dataRelay.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.favId == it.favId }
                    if (index == -1) {
                        dataRelay.accept(getItems())
                    } else {
                        currentItems[index] = newItem.toDomain()
                        dataRelay.accept(currentItems)
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