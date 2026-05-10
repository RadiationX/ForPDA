package forpdateam.ru.forpda.client

import android.util.Log
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.coroutines.executeAsync
import java.util.concurrent.TimeUnit

class Client(
    private val cookieJar: AppCookieJar,
    private val countersHolder: CountersHolder
) : IWebClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .cookieJar(cookieJar)
        .build()

    private val webSocketClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .cookieJar(cookieJar)
        .build()

    //Network
    @Throws(Exception::class)
    override suspend fun get(url: String): NetworkResponse {
        return request(NetworkRequest.Builder().url(url).build())
    }

    @Throws(Exception::class)
    override suspend fun request(request: NetworkRequest): NetworkResponse {
        return request(request, this.client, null)
    }

    @Throws(Exception::class)
    override suspend fun request(
        request: NetworkRequest,
        progressListener: IWebClient.ProgressListener
    ): NetworkResponse {
        return request(request, this.client, progressListener)
    }

    private fun prepareRequest(
        request: NetworkRequest,
        uploadProgressListener: IWebClient.ProgressListener?
    ): Request.Builder {
        var url = request.url
        if (request.url.startsWith("//")) {
            url = "https:" + request.url
        }
        Log.d(LOG_TAG, "Request url " + request.url)
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
            .header("User-Agent", USER_AGENT)
        if (request.headers != null) {
            for ((key, value) in request.headers) {
                Log.d(LOG_TAG, "Header $key : " + getPrivateHeaderValue(key, value))
                requestBuilder.header(key, value)
            }
        }
        if (request.formHeaders != null || request.file != null) {
            Log.d(LOG_TAG, "Multipart " + request.isMultipartForm)
            if (request.formHeaders != null) {
                for ((key, value) in request.formHeaders) {
                    Log.d(LOG_TAG, "Form header $key : " + getPrivateHeaderValue(key, value))
                }
            }
            if (request.file != null) {
                Log.d(LOG_TAG, "Form file " + request.file.toString())
            }
            if (!request.isMultipartForm) {
                if (request.formHeaders != null) {
                    val formBuilder = FormBody.Builder()
                    for ((key, value) in request.formHeaders) {
                        formBuilder.add(key, value)
                        if (request.encodedFormHeaders != null && request.encodedFormHeaders.contains(
                                key
                            )
                        ) {
                            formBuilder.addEncoded(key, value)
                        } else {
                            formBuilder.add(key, value)
                        }
                    }
                    val formBody = formBuilder.build()
                    requestBuilder.post(formBody)
                }
            } else {
                val multipartBuilder = MultipartBody.Builder()
                multipartBuilder.setType(MultipartBody.FORM)
                if (request.formHeaders != null) {
                    for ((key, value) in request.formHeaders) {
                        multipartBuilder.addFormDataPart(key, value)
                    }
                }
                request.file?.also { file ->
                    val type = file.mimeType.toMediaTypeOrNull()
                    val requestBody = RequestBodyUtil
                        .create(type, file.fileStream)
                    multipartBuilder.addFormDataPart(
                        file.requestName!!,
                        file.fileName,
                        requestBody
                    )
                }
                val multipartBody = multipartBuilder.build()
                if (uploadProgressListener == null) {
                    requestBuilder.post(multipartBody)
                } else {
                    requestBuilder.post(ProgressRequestBody(multipartBody, uploadProgressListener))
                }
            }
        }
        return requestBuilder
    }

    @Throws(Exception::class)
    private suspend fun request(
        request: NetworkRequest,
        client: OkHttpClient,
        uploadProgressListener: IWebClient.ProgressListener?
    ): NetworkResponse {
        val requestBuilder = prepareRequest(request, uploadProgressListener)

        val call = client.newCall(requestBuilder.build())

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
                request.url,
                response.code,
                response.message,
                response.request.url.toString(),
                body
            )
        }
    }

    override fun createWebSocketConnection(webSocketListener: WebSocketListener): WebSocket {
        val request = Request.Builder()
            .url("ws://app.4pda.to/ws/")
            .build()
        return webSocketClient.newWebSocket(request, webSocketListener)
    }

    @Throws(Exception::class)
    private fun checkForumErrors(res: String) {
        val errorMatcher = IWebClient.errorPattern.matcher(res)
        if (errorMatcher.find()) {
            throw OnlyShowException(ApiUtils.fromHtml(errorMatcher.group(1)))
        }
    }

    private fun getCounts(res: String) {
        val countsMatcher = IWebClient.countsPattern.matcher(res)

        if (countsMatcher.find()) {
            try {
                val counters = MessageCounters(
                    mentions = countsMatcher.group(1)?.toInt() ?: 0,
                    favorites = countsMatcher.group(2)?.toInt() ?: 0,
                    qms = countsMatcher.group(3)?.toInt() ?: 0
                )
                countersHolder.set(counters)
            } catch (exception: Exception) {
                Log.d("WATAFUCK", res, exception)
            }
        }
    }

    private fun getPrivateHeaderValue(key: String, value: String): String {
        return if (key in CookieStorage.AUTH_COOKIES) {
            "private"
        } else {
            value
        }
    }

    companion object {
        private val LOG_TAG = Client::class.java.simpleName
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36"
    }
}
