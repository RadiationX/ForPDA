package forpdateam.ru.forpda.model.data.cache.favorites

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import io.reactivex.Observable
import io.realm.Realm

class FavoritesCache {

    private val dataRelay = BehaviorRelay.create<List<FavItem>>()

    fun observeItems(): Observable<List<FavItem>> = dataRelay.hide()

    fun getItems(): List<FavItem> = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).findAll().map { FavItem(it) }
    }.also {
        if (!dataRelay.hasValue()) {
            dataRelay.accept(it)
        }
    }

    fun saveFavorites(items: List<FavItem>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(FavItemBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { FavItemBd(it) })
        }
        dataRelay.accept(getItems())
    }

    fun getItemByFavId(favId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("favId", favId).findFirst()?.let {
            FavItem(it)
        }
    }

    fun getItemByTopicId(topicId: Int): FavItem? = Realm.getDefaultInstance().use { realm ->
        realm.where(FavItemBd::class.java).equalTo("topicId", topicId).findFirst()?.let {
            FavItem(it)
        }
    }

    fun updateItem(item: FavItem) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(FavItemBd::class.java).equalTo("favId", item.favId).findFirst()?.let {
                realmTr.copyToRealmOrUpdate(FavItemBd(item))
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
                            currentItems[index] = FavItem(newItem)
                            dataRelay.accept(currentItems)
                        }
                    }
        }
    }

}