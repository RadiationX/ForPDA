package forpdateam.ru.forpda.model.repository.faviorites

import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class FavoritesRepository @Inject constructor(
    private val favoritesApi: FavoritesApi,
    private val favoritesCache: FavoritesCache
) {

    fun observeItems(): Flow<List<Favorite>> {
        return favoritesCache.observeItems()
    }

    suspend fun loadFavorites(st: Int, all: Boolean, sorting: Sorting): FavoritesData {
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
}
