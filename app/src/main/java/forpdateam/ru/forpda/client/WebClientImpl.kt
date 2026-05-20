package forpdateam.ru.forpda.client

import android.content.Context
import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import forpdateam.ru.forpda.model.data.remote.api.common.GlobalParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.coroutines.executeAsync
import javax.inject.Inject

class WebClientImpl @Inject constructor(
    private val context: Context,
    private val client: OkHttpClient,
    private val countersHolder: CountersHolder,
    private val globalParser: GlobalParser
) : WebClient {

    private val mapper = NetworkRequestMapper(context)

    override suspend fun request(request: ApiRequest): NetworkResponse {
        return request(request.buildNetworkRequest())
    }

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

    private fun checkForumErrors(res: String) {
        val error = globalParser.parseForumError(res) ?: return
        throw OnlyShowException(error)
    }

    private fun getCounts(response: String) {
        val counters = globalParser.parseCounters(response) ?: return
        countersHolder.set(counters)
    }
}
