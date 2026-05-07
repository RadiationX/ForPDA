package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.client.websocket.WebSocketControllerNew
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Created by radiationx on 31.07.17.
 */
class WebSocketEventsApi(
    private val webSocketController: WebSocketControllerNew,
    private val parser: WebSocketEventParser
) {

    fun observeEvents(): Flow<WebSocketEvent> {
        return webSocketController
            .observeMessages()
            .mapNotNull { parser.parseWebSocketEvent(it) }
    }

}
