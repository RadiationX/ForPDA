package forpdateam.ru.forpda.model.data.remote.api.favorites

import android.net.Uri
import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.util.Collections
import kotlin.Boolean
import kotlin.Comparator
import kotlin.Int
import kotlin.String
import kotlin.arrayOf

/**
 * Created by radiationx on 22.09.16.
 */

class FavoritesApi(
        private val webClient: IWebClient,
        private val favoritesParser: FavoritesParser
) {

    fun getFavorites(st: Int, all: Boolean, sorting: Sorting): FavData {
        val uriBuilder = Uri.Builder()
                .scheme("https")
                .authority("4pda.ru")
                .appendPath("forum")
                .appendQueryParameter("act", "fav")
                .appendQueryParameter("type", "all")
                .appendQueryParameter("st", st.toString())
                .appendQueryParameter(Sorting.Key.HEADER, sorting.key)
                .appendQueryParameter(Sorting.Order.HEADER, sorting.order)

        val response = webClient.get(uriBuilder.build().toString())

        val data = favoritesParser.parseFavorites(response.body)

        if (all) {
            while (true) {
                if (data.pagination.current >= data.pagination.all) {
                    break
                }
                val favData = getFavorites(data.pagination.getPage(data.pagination.current), false, sorting)
                data.pagination = favData.pagination
                if (favData.items.isEmpty()) {
                    break
                }
                data.items.addAll(favData.items)
            }
            data.pagination.all = 1

            if (data.sorting.key == Sorting.Key.TITLE) {
                if (data.sorting.order == Sorting.Order.DESC) {
                    Collections.sort(data.items, DESC_ORDER)
                } else if (data.sorting.order == Sorting.Order.ASC) {
                    Collections.sort(data.items, ASC_ORDER)
                }
            }
        }

        return data
    }

    fun editSubscribeType(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val response = webClient.get("https://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=$type&selectedtids=$favId")
        return favoritesParser.checkIsComplete(response.body)
    }

    fun editPinState(type: String?, favId: Int): Boolean {
        checkNotNull(type)
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=fav")
                .formHeader("selectedtids", favId.toString())
                .formHeader("tact", type)
        val response = webClient.request(builder.build())
        return favoritesParser.checkIsComplete(response.body)
    }

    fun delete(favId: Int): Boolean {
        val builder = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=fav")
                .xhrHeader()
                .formHeader("selectedtids", favId.toString())
                .formHeader("tact", "delete")
        val response = webClient.request(builder.build())
        return favoritesParser.checkIsComplete(response.body)
    }

    fun add(id: Int, action: Int, type: String?): Boolean {
        checkNotNull(type)
        var url = "https://4pda.ru/forum/index.php?act=fav&type=add&track_type=$type"
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

        private val DESC_ORDER = Comparator<FavItem> { item1, item2 ->
            item1.topicTitle.orEmpty().compareTo(item2.topicTitle.orEmpty(), ignoreCase = true)
        }
        private val ASC_ORDER = Comparator<FavItem> { item1, item2 ->
            item2.topicTitle.orEmpty().compareTo(item1.topicTitle.orEmpty(), ignoreCase = true)
        }
    }
}
