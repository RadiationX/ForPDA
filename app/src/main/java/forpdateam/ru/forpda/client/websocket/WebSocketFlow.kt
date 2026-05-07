package forpdateam.ru.forpda.client.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketFlow(
    private val webSocketCreator: (WebSocketListener) -> WebSocket
) : Flow<WebSocketFlow.Event> {

    private val flow = callbackFlow<Event> {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WSListener onOpen")
                trySend(Event.Open(webSocket))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("WSListener onMessage")
                trySend(Event.TextMessage(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WSListener onClosing")
                webSocket.close(1000, null)
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WSListener onFailure $t")
                close(t)
            }
        }

        val webSocket = webSocketCreator.invoke(listener)
        awaitClose {
            webSocket.cancel()
        }
    }

    override suspend fun collect(collector: FlowCollector<Event>) {
        flow.collect(collector)
    }

    sealed interface Event {
        class Open(val webSocket: WebSocket) : Event
        data class TextMessage(val text: String) : Event
    }
}
