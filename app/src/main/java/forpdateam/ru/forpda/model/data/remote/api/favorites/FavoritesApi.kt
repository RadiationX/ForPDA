package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 22.09.16.
 */

class FavoritesApi @Inject constructor(
    private val webClient: WebClient,
    private val favoritesParser: FavoritesParser
) {

    suspend fun getFavorites(st: Int, all: Boolean, sorting: Sorting): FavoritesData {
        var data = getFavorites(st, sorting)
        if (all) {
            while (data.pagination.hasNext()) {
                val page = data.pagination.nextPage()
                val favData = getFavorites(page, sorting)
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

    private suspend fun getFavorites(st: Int, sorting: Sorting): FavoritesData {
        val response = webClient.request(ApiRequest.Forum.Favorite.GetList(st, sorting))
        return favoritesParser.parseFavorites(response.body)
    }

    suspend fun editSubscribeType(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val response = webClient.request(ApiRequest.Forum.Favorite.EditTrackType(favId, type))
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun editPinState(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val response = webClient.request(ApiRequest.Forum.Favorite.EditPinState(favId, type))
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun delete(favId: Int): Boolean {
        val response = webClient.request(ApiRequest.Forum.Favorite.Delete(favId))
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun add(id: Int, action: Int, type: String?): Boolean {
        checkNotNull(type)
        val request = when (action) {
            ACTION_ADD_FORUM -> ApiRequest.Forum.Favorite.Add.Forum(id, type)
            ACTION_ADD -> ApiRequest.Forum.Favorite.Add.Topic(id, type)
            else -> null
        }
        requireNotNull(request)
        val response = webClient.request(request)
        return favoritesParser.checkIsComplete(response.body)
    }

    companion object {

        const val ACTION_EDIT_SUB_TYPE = 0
        const val ACTION_EDIT_PIN_STATE = 1
        const val ACTION_DELETE = 2
        const val ACTION_ADD = 3
        const val ACTION_ADD_FORUM = 4
        val SUB_TYPES = arrayOf("none", "delayed", "immediate", "daily", "weekly", "pinned")

        private val DESC_ORDER = Comparator<Favorite> { item1, item2 ->
            item1.title.compareTo(item2.title, ignoreCase = true)
        }
        private val ASC_ORDER = Comparator<Favorite> { item1, item2 ->
            item2.title.compareTo(item1.title, ignoreCase = true)
        }
    }
}
