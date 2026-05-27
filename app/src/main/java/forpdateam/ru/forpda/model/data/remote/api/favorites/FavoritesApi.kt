package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoriteAction
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.PageOffset
import javax.inject.Inject

/**
 * Created by radiationx on 22.09.16.
 */

class FavoritesApi @Inject constructor(
    private val webClient: WebClient,
    private val favoritesParser: FavoritesParser
) {

    suspend fun getFavorites(offset: PageOffset, all: Boolean, sorting: Sorting): FavoritesData {
        var data = getFavorites(offset, sorting)
        if (all) {
            while (data.pagination.hasNext()) {
                val page = data.pagination.nextPage()
                val favData = getFavorites(PageOffset(page), sorting)
                data = data.copy(
                    pagination = favData.pagination,
                    items = data.items + favData.items
                )
                if (favData.items.isEmpty()) {
                    break
                }
            }
            val finalPagination = data.pagination.copy(
                perPage = data.items.size,
                current = 1,
                all = 1
            )

            val finalItems = if (data.sorting.key == Sorting.Key.TITLE) {
                when (data.sorting.order) {
                    Sorting.Order.DESC -> data.items.sortedWith(DESC_ORDER)
                    Sorting.Order.ASC -> data.items.sortedWith(ASC_ORDER)
                    else -> data.items
                }
            } else {
                data.items
            }
            data = data.copy(
                pagination = finalPagination,
                items = finalItems
            )
        }
        return data
    }

    private suspend fun getFavorites(offset: PageOffset, sorting: Sorting): FavoritesData {
        val response = webClient.request(ApiRequest.Forum.Favorite.GetList(offset, sorting))
        return favoritesParser.parseFavorites(response.body)
    }

    suspend fun editFavorites(action: FavoriteAction): Boolean {
        val request = when (action) {
            is FavoriteAction.AddTopic -> ApiRequest.Forum.Favorite.Add.Topic(action.topicId, action.trackType)
            is FavoriteAction.AddForum -> ApiRequest.Forum.Favorite.Add.Forum(action.forumId, action.trackType)
            is FavoriteAction.Delete -> ApiRequest.Forum.Favorite.Delete(action.favoriteId)
            is FavoriteAction.EditPinState -> ApiRequest.Forum.Favorite.EditPinState(action.favoriteId, action.state)
            is FavoriteAction.EditTrackType -> ApiRequest.Forum.Favorite.EditTrackType(action.favoriteId, action.trackType)
        }
        val response = webClient.request(request)
        return favoritesParser.checkIsComplete(response.body)
    }

    companion object {
        val SUB_TYPES = arrayOf("none", "delayed", "immediate", "daily", "weekly", "pinned")

        private val DESC_ORDER = Comparator<Favorite> { item1, item2 ->
            item1.title.compareTo(item2.title, ignoreCase = true)
        }
        private val ASC_ORDER = Comparator<Favorite> { item1, item2 ->
            item2.title.compareTo(item1.title, ignoreCase = true)
        }
    }
}
