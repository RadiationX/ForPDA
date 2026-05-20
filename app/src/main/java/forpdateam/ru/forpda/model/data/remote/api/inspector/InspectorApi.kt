package forpdateam.ru.forpda.model.data.remote.api.inspector

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.inspector.InspectorMention
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.17.
 */
class InspectorApi @Inject constructor(
    private val webClient: WebClient,
    private val parser: InspectorParser
) {

    suspend fun getFavorites(): List<InspectorItem.Favorite> {
        val response = webClient.request(ApiRequest.Forum.Inspector.Favorites)
        return parser.parseFavoritesEvents(response.body)
    }

    suspend fun getQms(): List<InspectorItem.Qms> {
        val response = webClient.request(ApiRequest.Forum.Inspector.Qms)
        return parser.parseQmsEvents(response.body)
    }

    suspend fun getMentionsCount(): InspectorMention {
        val response = webClient.request(ApiRequest.Forum.Inspector.Mentions)
        return InspectorMention(response.body.toInt())
    }

}