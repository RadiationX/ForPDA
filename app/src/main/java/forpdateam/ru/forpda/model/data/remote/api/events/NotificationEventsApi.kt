package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.model.data.remote.IWebClient

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationEventsApi(
    private val webClient: IWebClient,
    private val parser: NotificationEventsParser
) {

    suspend fun getFavoritesEvents(): List<NotificationEvent> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=fav")
        return parser.parseFavoritesEvents(response.body)
    }

    suspend fun getQmsEvents(): List<NotificationEvent> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=qms")
        return parser.parseQmsEvents(response.body)
    }

}
