package forpdateam.ru.forpda.model.data.remote

import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.Cookie
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.regex.Pattern

/**
 * Created by radiationx on 26.03.17.
 */
interface IWebClient {
    @Throws(Exception::class)
    fun get(url: String): NetworkResponse

    @Throws(Exception::class)
    fun request(request: NetworkRequest): NetworkResponse

    @Throws(Exception::class)
    fun request(request: NetworkRequest, progressListener: ProgressListener): NetworkResponse

    fun getAuthKey(): String

    fun getClientCookies(): Map<String, Cookie>

    fun clearCookies()

    fun createWebSocketConnection(webSocketListener: WebSocketListener): WebSocket

    fun interface ProgressListener {
        fun onProgress(percent: Int)
    }

    companion object {
        val countsPattern: Pattern =
            Pattern.compile("<a href=\"(?:https?)?\\/\\/4pda\\.(?:ru|to)\\/forum\\/index\\.php\\?act=mentions\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?act=fav&amp;code=no\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?span id=\"events-count\"[\\s\\S]*?(?:data-count=\"(\\d+)\")")
        val errorPattern: Pattern =
            Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">")
        const val MINIMAL_PAGE: String = "https://4pda.to/forum/index.php?showforum=200#afterauth"
    }
}
