package forpdateam.ru.forpda.model.data.remote

import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Created by radiationx on 26.03.17.
 */
interface WebClient {

    suspend fun get(url: String): NetworkResponse

    suspend fun request(request: NetworkRequest): NetworkResponse

    fun createWebSocketConnection(webSocketListener: WebSocketListener): WebSocket

    fun interface ProgressListener {
        fun onProgress(percent: Int)
    }
}
