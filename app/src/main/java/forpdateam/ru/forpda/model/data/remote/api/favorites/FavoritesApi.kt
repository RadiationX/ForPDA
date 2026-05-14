package forpdateam.ru.forpda.model.data.remote.api.favorites

import android.net.Uri
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest

/**
 * Created by radiationx on 22.09.16.
 */

class FavoritesApi(
    private val webClient: IWebClient,
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
        val uriBuilder = Uri.Builder()
            .scheme("https")
            .authority("4pda.to")
            .appendPath("forum")
            .appendQueryParameter("act", "fav")
            .appendQueryParameter("type", "all")
            .appendQueryParameter("st", st.toString())
            .appendQueryParameter(Sorting.Key.HEADER, sorting.key)
            .appendQueryParameter(Sorting.Order.HEADER, sorting.order)

        val response = webClient.get(uriBuilder.build().toString())
        return favoritesParser.parseFavorites(response.body)
    }

    suspend fun editSubscribeType(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val response =
            webClient.get("https://4pda.to/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=$type&selectedtids=$favId")
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun editPinState(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=fav")
            .formHeader("selectedtids", favId.toString())
            .formHeader("tact", type)
        val response = webClient.request(builder.build())
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun delete(favId: Int): Boolean {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=fav")
            .xhrHeader()
            .formHeader("selectedtids", favId.toString())
            .formHeader("tact", "delete")
        val response = webClient.request(builder.build())
        return favoritesParser.checkIsComplete(response.body)
    }

    suspend fun add(id: Int, action: Int, type: String?): Boolean {
        checkNotNull(type)
        var url = "https://4pda.to/forum/index.php?act=fav&type=add&track_type=$type"
        if (action == ACTION_ADD_FORUM) {
            url += "&f="
        } else if (action == ACTION_ADD) {
            url += "&t="
        }
        url += id
        val response = webClient.request(NetworkRequest.Builder().url(url).build())
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
