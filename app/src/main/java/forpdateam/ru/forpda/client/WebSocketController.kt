package forpdateam.ru.forpda.client

import android.util.Log
import forpdateam.ru.forpda.model.data.remote.IWebClient
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketController(
    private val webClient: IWebClient,
    private val listener: Listener
) {

    companion object {
        private const val LOG_TAG = "WebSocketController"
    }

    private var currentWebSocket: WebSocket? = null
    private var currentId: Int = 0
    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private fun createWebSocketListener(): WebSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(LOG_TAG, "WSListener onOpen")
            connectionState.value = ConnectionState.Connected
            listener.onConnected()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(LOG_TAG, "WSListener onMessage: $text")
            connectionState.value = ConnectionState.Connected
            listener.onMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(LOG_TAG, "WSListener onClosing: $code, $reason")
            connectionState.value = ConnectionState.Disconnected
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(LOG_TAG, "WSListener onClosing: $code, $reason")
            connectionState.value = ConnectionState.Disconnected
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(LOG_TAG, "WSListener onFailure: $response;", t)
            connectionState.value = ConnectionState.Disconnected
            listener.onDisconnected(t, response)
        }
    }

    fun connect() {
        disconnect()
        val newId = (1000..16384).random()
        val newWebSocket = webClient.createWebSocketConnection(createWebSocketListener())
        currentWebSocket = newWebSocket
        currentId = newId
        connectionState.value = ConnectionState.Connecting
    }

    fun send(message: String) {
        currentWebSocket?.send(message)
    }

    fun disconnect() {
        currentWebSocket?.cancel()
        currentWebSocket = null
        connectionState.value = ConnectionState.Disconnected
    }

    fun isConnected(): Boolean {
        return connectionState.value == ConnectionState.Connected
    }

    fun getCurrentId() = currentId

    open class Listener {
        open fun onConnected() {}
        open fun onDisconnected(throwable: Throwable, response: Response?) {}
        open fun onMessage(text: String) {}
    }

    enum class ConnectionState {
        Disconnected,
        Connecting,
        Connected
    }
}