package forpdateam.ru.forpda.model.repository.faviorites

import android.util.Log
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.preferences.ListsPreferencesHolder
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class FavoritesRepository(
        private val schedulers: SchedulersProvider,
        private val favoritesApi: FavoritesApi,
        private val favoritesCache: FavoritesCache,
        private val authHolder: AuthHolder,
        private val countersHolder: CountersHolder,
        private val listsPreferencesHolder: ListsPreferencesHolder,
        private val notificationPreferencesHolder: NotificationPreferencesHolder
) : BaseRepository(schedulers) {


    fun observeItems(): Observable<List<FavItem>> = favoritesCache
            .observeItems()
            .runInIoToUi()

    fun loadCache(): Single<List<FavItem>> = Single
            .fromCallable { favoritesCache.getItems() }
            .runInIoToUi()

    fun loadFavorites(st: Int, all: Boolean, sorting: Sorting): Single<FavData> = Single
            .fromCallable { favoritesApi.getFavorites(st, all, sorting) }
            .doOnSuccess { favData -> favoritesCache.saveFavorites(favData.items) }
            .runInIoToUi()

    fun editFavorites(act: Int, favId: Int, id: Int, type: String?): Single<Boolean> = Single
            .fromCallable {
                when (act) {
                    FavoritesApi.ACTION_EDIT_SUB_TYPE -> favoritesApi.editSubscribeType(type, favId)
                    FavoritesApi.ACTION_EDIT_PIN_STATE -> favoritesApi.editPinState(type, favId)
                    FavoritesApi.ACTION_DELETE -> favoritesApi.delete(favId)
                    FavoritesApi.ACTION_ADD, FavoritesApi.ACTION_ADD_FORUM -> favoritesApi.add(id, act, type)
                    else -> false
                }
            }
            .runInIoToUi()

    fun markRead(topicId: Int): Completable = Completable
            .fromRunnable {
                val favItem = favoritesCache.getItemByTopicId(topicId)
                if (favItem != null) {
                    favItem.isNew = false
                    favoritesCache.updateItem(favItem)
                }
            }
            .runInIoToUi()


    fun handleEvent(event: TabNotification): Single<Int> = Single
            .fromCallable {
                val favItems = favoritesCache.getItems()
                val sorting = Sorting(
                        listsPreferencesHolder.getSortingKey(),
                        listsPreferencesHolder.getSortingOrder()
                )
                val count = countersHolder.get().favorites
                handleEventTransaction(favItems, event, sorting, count).also {
                    countersHolder.set(countersHolder.get().apply {
                        favorites = it
                    })
                }
            }
            .runInIoToUi()


    private fun handleEventTransaction(favItems: List<FavItem>, event: TabNotification, sorting: Sorting, count: Int): Int {
        if (!NotificationEvent.fromTheme(event.source)) return count
        if (!notificationPreferencesHolder.getFavLiveTab()) return count
        if (event.isWebSocket && event.event.isNew) return count

        var newCount = count
        val newFavItems = favItems.toMutableList()
        val loadedEvent = event.event
        val topicId = loadedEvent.sourceId
        val isRead = loadedEvent.isRead

        Log.e("testtabnotify", "handleEventTransaction $newCount, $topicId, $isRead, ${loadedEvent.userNick}")

        if (isRead) {
            newFavItems.find { it.topicId == topicId }?.also {
                if (it.isNew) {
                    newCount--
                    it.isNew = false
                }
                Log.e("testtabnotify", "found item ${it.isNew}, $newCount")
            }
        } else {
            newCount = event.loadedEvents.size
            Log.e("testtabnotify", "lalala $newCount")
            newFavItems.find { it.topicId == topicId }?.also {
                if (it.lastUserId != authHolder.get().userId) {
                    it.isNew = true
                }
                it.lastUserNick = loadedEvent.userNick
                it.lastUserId = loadedEvent.userId
                it.isPin = loadedEvent.isImportant
            }
            if (sorting.key == Sorting.Key.TITLE) {
                if (sorting.order == Sorting.Order.ASC) {
                    newFavItems.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.topicTitle.orEmpty() })
                } else {
                    newFavItems.sortWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.topicTitle.orEmpty() })
                }
            }

            if (sorting.key == Sorting.Key.LAST_POST) {
                newFavItems.find { it.topicId == topicId }?.also {
                    newFavItems.remove(it)
                    if (sorting.order == Sorting.Order.ASC) {
                        newFavItems.add(newFavItems.size, it)
                    } else {
                        newFavItems.add(0, it)
                    }
                }
            }
        }
        favoritesCache.saveFavorites(newFavItems)
        return newCount
    }
}
