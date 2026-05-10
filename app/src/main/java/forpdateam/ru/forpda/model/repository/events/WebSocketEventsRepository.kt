package forpdateam.ru.forpda.model.repository.events

import forpdateam.ru.forpda.client.websocket.WebSocketController
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.model.data.remote.api.events.WebSocketEventParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Created by radiationx on 31.07.17.
 */
class WebSocketEventsRepository(
    private val webSocketController: WebSocketController,
    private val parser: WebSocketEventParser
) {

    fun observeEvents(): Flow<WebSocketEvent> {
        return webSocketController
            .observeMessages()
            .mapNotNull { parser.parseWebSocketEvent(it) }
    }

    suspend fun sendMessage(text: String){
        webSocketController.sendMessage(text)
    }

}