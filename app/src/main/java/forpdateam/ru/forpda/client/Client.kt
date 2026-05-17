package forpdateam.ru.forpda.client

import android.content.Context
import android.util.Log
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.coroutines.executeAsync
import javax.inject.Inject

class Client @Inject constructor(
    private val context: Context,
    private val client: OkHttpClient,
    private val countersHolder: CountersHolder
) : IWebClient {

    private val mapper = NetworkRequestMapper(context)

    //Network
    @Throws(Exception::class)
    override suspend fun get(url: String): NetworkResponse {
        return request(NetworkRequest.Builder().url(url).build())
    }

    @Throws(Exception::class)
    override suspend fun request(request: NetworkRequest): NetworkResponse {
        val okHttpRequest = mapper.map(request)
        val call = client.newCall(okHttpRequest)
        return call.executeAsync().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 403) {
                    val content = response.body.string()
                    //todo catch this is errorhandler
                    throw GoogleCaptchaException(content)
                }
                throw OkHttpResponseException(
                    response.code,
                    response.message,
                    request.url
                )
            }

            val body = if (request.isWithoutBody) {
                ""
            } else {
                response.body.string()
            }

            getCounts(body)
            checkForumErrors(body)

            NetworkResponse(
                url = request.url,
                code = response.code,
                message = response.message,
                redirect = response.request.url.toString(),
                body = body
            )
        }
    }

    override fun createWebSocketConnection(webSocketListener: WebSocketListener): WebSocket {
        val request = Request.Builder()
            .url("ws://app.4pda.to/ws/")
            .build()
        return client.newWebSocket(request, webSocketListener)
    }

    @Throws(Exception::class)
    private fun checkForumErrors(res: String) {
        val errorMatcher = IWebClient.errorPattern.matcher(res)
        if (errorMatcher.find()) {
            throw OnlyShowException(ApiUtils.fromHtml(errorMatcher.group(1)))
        }
    }

    private fun getCounts(response: String) {
        val countsMatcher = IWebClient.countsPattern.matcher(response)

        if (countsMatcher.find()) {
            try {
                val counters = MessageCounters(
                    mentions = countsMatcher.group(1)?.toInt() ?: 0,
                    favorites = countsMatcher.group(2)?.toInt() ?: 0,
                    qms = countsMatcher.group(3)?.toInt() ?: 0
                )
                countersHolder.set(counters)
            } catch (exception: Exception) {
                Log.d("WATAFUCK", response, exception)
            }
        }
    }
}
