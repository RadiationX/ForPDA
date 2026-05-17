package forpdateam.ru.forpda.model.data.remote.api.inspector

import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.inspector.InspectorMention
import forpdateam.ru.forpda.model.data.remote.IWebClient
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.17.
 */
class InspectorApi @Inject constructor(
    private val webClient: IWebClient,
    private val parser: InspectorParser
) {

    suspend fun getFavorites(): List<InspectorItem.Favorite> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=fav")
        return parser.parseFavoritesEvents(response.body)
    }

    suspend fun getQms(): List<InspectorItem.Qms> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=qms")
        return parser.parseQmsEvents(response.body)
    }

    suspend fun getMentionsCount(): InspectorMention {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=mentions")
        return InspectorMention(response.body.toInt())
    }

}