package forpdateam.ru.forpda.model.repository.faviorites

import android.util.Log
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.extensions.replace
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.preferences.ListsPreferencesHolder
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 01.01.18.
 */

class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
    private val favoritesCache: FavoritesCache,
    private val authHolder: AuthHolder,
    private val countersHolder: CountersHolder,
    private val listsPreferencesHolder: ListsPreferencesHolder,
    private val notificationPreferencesHolder: NotificationPreferencesHolder
) {

    fun observeItems(): Flow<List<FavItem>> {
        return favoritesCache.observeItems()
    }

    suspend fun loadCache(): List<FavItem> {
        return favoritesCache.getItems()
    }

    suspend fun loadFavorites(st: Int, all: Boolean, sorting: Sorting): FavData {
        return favoritesApi.getFavorites(st, all, sorting).also {
            favoritesCache.saveFavorites(it.items)
        }
    }

    suspend fun editFavorites(act: Int, favId: Int, id: Int, type: String?): Boolean {
        return when (act) {
            FavoritesApi.ACTION_EDIT_SUB_TYPE -> {
                favoritesApi.editSubscribeType(type, favId)
            }

            FavoritesApi.ACTION_EDIT_PIN_STATE -> {
                favoritesApi.editPinState(type, favId)
            }

            FavoritesApi.ACTION_DELETE -> {
                favoritesApi.delete(favId)
            }

            FavoritesApi.ACTION_ADD, FavoritesApi.ACTION_ADD_FORUM -> {
                favoritesApi.add(id, act, type)
            }

            else -> {
                false
            }
        }
    }

    suspend fun markRead(topicId: Int) {
        favoritesCache.getItemByTopicId(topicId)?.also {
            favoritesCache.updateItem(it.copy(isNew = false))
        }
    }

    suspend fun handleEvent(event: TabNotification): Int {
        val favItems = favoritesCache.getItems()
        val sorting = Sorting(
            listsPreferencesHolder.getSortingKey(),
            listsPreferencesHolder.getSortingOrder()
        )
        val count = countersHolder.get().favorites
        return handleEventTransaction(favItems, event, sorting, count).also {
            countersHolder.set(
                countersHolder.get().copy(
                    favorites = it
                )
            )
        }
    }

    private suspend fun handleEventTransaction(
        favItems: List<FavItem>,
        event: TabNotification,
        sorting: Sorting,
        count: Int
    ): Int {
        if (!NotificationEvent.fromTheme(event.source)) return count
        if (!notificationPreferencesHolder.getFavLiveTab()) return count
        if (event.isWebSocket && event.event.isNew) return count

        var newCount = count
        val newFavItems = favItems.toMutableList()
        val loadedEvent = event.event
        val topicId = loadedEvent.sourceId
        val isRead = loadedEvent.isRead

        Log.e(
            "testtabnotify",
            "handleEventTransaction $newCount, $topicId, $isRead, ${loadedEvent.user?.nick}"
        )

        if (isRead) {
            newFavItems.replace(
                condition = { it.topicId == topicId },
                map = {
                    if (it.isNew) {
                        newCount--
                    }
                    Log.e("testtabnotify", "found item ${it.isNew}, $newCount")
                    it.copy(isNew = false)
                }
            )
        } else {
            newCount = event.loadedEvents.size
            Log.e("testtabnotify", "lalala $newCount")
            newFavItems.replace(
                condition = { it.topicId == topicId },
                map = {
                    it.copy(
                        isNew = it.lastUser.id != authHolder.get().userId,
                        lastUser = loadedEvent.user ?: it.lastUser,
                        isPin = loadedEvent.isImportant
                    )
                }
            )
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
