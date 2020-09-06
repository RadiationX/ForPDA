package forpdateam.ru.forpda.client

import android.util.Log
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.lang.Exception
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeoutException

class WebSocketController(
        private val webClient: IWebClient,
        private val listener: Listener
) {

    private val webSockets = mutableListOf<WebSocketState>()
    private var currentId = NO_ID

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            val eventWebSocket = getByWebSocket(webSocket)
            val currentWebSocket = getById(currentId)
            Log.d(LOG_TAG, "WSListener onOpen; ${eventWebSocket?.id}, ${currentWebSocket?.id}")
            eventWebSocket?.connected = true
            if (currentWebSocket == eventWebSocket) {
                listener.onConnected()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String?) {
            val eventWebSocket = getByWebSocket(webSocket)
            val currentWebSocket = getById(currentId)
            Log.d(LOG_TAG, "WSListener onMessage: $text; ${eventWebSocket?.id}, ${currentWebSocket?.id}")
            eventWebSocket?.connected = true
            if (currentWebSocket == eventWebSocket) {
                listener.onMessage(text)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String?) {}

        override fun onFailure(webSocket: WebSocket, throwable: Throwable, response: Response?) {
            val eventWebSocket = getByWebSocket(webSocket)
            val currentWebSocket = getById(currentId)
            Log.d(LOG_TAG, "WSListener onFailure: ${throwable.message} $response; ${eventWebSocket?.id}, ${currentWebSocket?.id}")
            eventWebSocket?.connected = false
            eventWebSocket?.also {
                try {
                    webSockets.remove(eventWebSocket)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            if (currentWebSocket == eventWebSocket) {
                listener.onDisconnected(throwable, response)
            }
        }
    }

    fun connect() {
        val newId = (1000..16384).random()
        val newWebSocket = webClient.createWebSocketConnection(webSocketListener)
        val newWebSocketState = WebSocketState(newId, newWebSocket, true)
        currentId = newId
        webSockets.add(newWebSocketState)
    }

    fun send(message: String) {
        getById(currentId)?.webSocket?.send(message)
    }

    fun disconnectAll() {
        webSockets.forEach {
            it.webSocket.cancel()
            it.connected = false
        }
    }

    fun isConnected(): Boolean {
        return (currentId != NO_ID && getById(currentId)?.connected ?: false).also {

            Log.d(LOG_TAG, "isConnected $currentId, ${getById(currentId)} ... $it")
        }
    }

    fun getCurrentId() = currentId

    private fun getById(id: Int): WebSocketState? = webSockets.firstOrNull { it.id == id }

    private fun getByWebSocket(webSocket: WebSocket): WebSocketState? = webSockets.firstOrNull { it.webSocket == webSocket }

    private fun IntRange.random() = Random().nextInt((endInclusive + 1) - start) + start

    companion object {
        const val NO_ID = -1
        private const val LOG_TAG = "WebSocketController"
    }

    open class Listener {
        open fun onConnected() {}
        open fun onDisconnected(throwable: Throwable, response: Response?) {}
        open fun onMessage(text: String?) {}
    }

    private class WebSocketState(
            var id: Int,
            var webSocket: WebSocket,
            var connected: Boolean = false
    ) {
        override fun toString(): String {
            return "WebSocketState[$id, $connected]"
        }
    }
}