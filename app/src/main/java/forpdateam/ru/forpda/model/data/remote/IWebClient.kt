package forpdateam.ru.forpda.model.data.remote

import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.regex.Pattern

/**
 * Created by radiationx on 26.03.17.
 */
interface IWebClient {

    suspend fun get(url: String): NetworkResponse

    suspend fun request(request: NetworkRequest): NetworkResponse

    fun createWebSocketConnection(webSocketListener: WebSocketListener): WebSocket

    fun interface ProgressListener {
        fun onProgress(percent: Int)
    }

    companion object {
        val countsPattern: Pattern =
            Pattern.compile("act=mentions\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?act=fav[^\"]*?\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?span id=\"events-count\"[\\s\\S]*?(?:data-count=\"(\\d+)\")")
        val errorPattern: Pattern =
            Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">")
        const val MINIMAL_PAGE: String = "https://4pda.to/forum/index.php?showforum=200#afterauth"
    }
}
