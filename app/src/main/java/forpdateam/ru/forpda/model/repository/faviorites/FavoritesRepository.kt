package forpdateam.ru.forpda.model.repository.faviorites

import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoriteAction
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import kotlinx.coroutines.flow.Flow
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.TopicId
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

    suspend fun loadFavorites(st: PageOffset, all: Boolean, sorting: Sorting): FavoritesData {
        return favoritesApi.getFavorites(st, all, sorting).also {
            favoritesCache.saveFavorites(it.items)
        }
    }

    suspend fun editFavorites(action: FavoriteAction): Boolean {
        return favoritesApi.editFavorites(action)
    }

    suspend fun markRead(topicId: TopicId) {
        favoritesCache.getItemByTopicId(topicId)?.also {
            favoritesCache.updateItem(it.copy(isNew = false))
        }
    }
}
