package forpdateam.ru.forpda.client

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.security.GeneralSecurityException
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

class Client(
    context: Context?,
    private val authHolder: AuthHolder,
    private val countersHolder: CountersHolder
) : IWebClient {
    private val clientCookies: MutableMap<String, Cookie> = HashMap()
    private val observerHandler = Handler(Looper.getMainLooper())
    private val privateHeaders: List<String> =
        ArrayList(mutableListOf("pass_hash", "session_id", "auth_key", "password"))
    private val mobileCookie = Cookie.parse(HttpUrl.parse("https://4pda.to/"), "ngx_mb=1;")

    override fun getAuthKey(): String {
        return get().preferences.getString("auth_key", null) ?: ""
    }

    private fun parseCookie(cookieFields: String): Cookie? {
        /*Хранение: Url|:|Cookie*/
        val fields =
            cookieFields.split("\\|:\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Cookie.parse(HttpUrl.parse(fields[0]), fields[1])
    }

    private fun cookieToPref(url: String, cookie: Cookie): String {
        return "$url|:|$cookie"
    }

    override fun getClientCookies(): Map<String, Cookie> {
        return clientCookies
    }

    private val cookieJar: CookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val editor = get().preferences.edit()
            /*for (Cookie cookie : cookies) {
                Log.e("SUKA", "save COOK " + cookie.name() + " : " + cookie.value());
            }*/
            for (cookie in cookies) {
                if (cookie.value() == "deleted") {
                    editor.remove("cookie_" + cookie.name())
                    clientCookies.remove(cookie.name())
                } else {
                    editor.putString(
                        "cookie_" + cookie.name(),
                        cookieToPref(url.toString(), cookie)
                    )
                    if (cookie.name() == "member_id") {
                        editor.putString("member_id", cookie.value())
                        val userId = cookie.value().toInt()
                        val authData = authHolder.get()
                        authData.userId = userId
                        authData.state =
                            if (userId == AuthData.NO_ID) AuthState.NO_AUTH else AuthState.AUTH
                        authHolder.set(authData)
                    }
                    if (!clientCookies.containsKey(cookie.name())) {
                        clientCookies.remove(cookie.name())
                    }
                    clientCookies[cookie.name()] = cookie
                }
            }
            editor.apply()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val external = !url.host().lowercase(Locale.getDefault()).contains("4pda")
            if (!external) {
                clientCookies["ngx_mb"] = mobileCookie!!
            }

            val cookies: MutableList<Cookie> = ArrayList(clientCookies.values)
            if (external) {
                for (privateName in privateHeaders) {
                    for (i in cookies.indices) {
                        if (cookies[i].name() == privateName) {
                            cookies.removeAt(i)
                            break
                        }
                    }
                }
            }
            /*for (Cookie cookie : cookies) {
                Log.e("SUKA", "load COOK " + cookie.name() + " : " + cookie.value());
            }*/
            return cookies
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .sslSocketFactory(newSslContext.socketFactory)
        .cookieJar(cookieJar)
        .build()

    private val webSocketClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(newSslContext.socketFactory)
        .retryOnConnectionFailure(true)
        .cookieJar(cookieJar)
        .build()


    //Контекст нужен, для чтения настроек
    //Не необходимо, но вдруг случится шо у App не будет контекста
    init {
        val authData = authHolder.get()
        val preferences = get().preferences
        val member_id = preferences.getString("cookie_member_id", null)
        val pass_hash = preferences.getString("cookie_pass_hash", null)
        val session_id = preferences.getString("cookie_session_id", null)
        val anonymous = preferences.getString("cookie_anonymous", null)
        val clearance = preferences.getString("cookie_cf_clearance", null)

        clientCookies["ngx_mb"] = mobileCookie!!
        if (clearance != null) {
            clientCookies["cf_clearance"] = parseCookie(clearance)!!
        }

        if (member_id != null && pass_hash != null) {
            val userId = preferences.getString("member_id", "0")!!.toInt()
            authData.state = AuthState.AUTH
            authData.userId = userId

            //Первичная загрузка кукисов
            clientCookies["member_id"] = parseCookie(member_id)!!
            clientCookies["pass_hash"] = parseCookie(pass_hash)!!
            if (session_id != null) clientCookies["session_id"] = parseCookie(session_id)!!
            if (anonymous != null) {
                clientCookies["anonymous"] = parseCookie(anonymous)!!
            }
        } else {
            authData.state = AuthState.SKIP
            authData.userId = 0
        }
        authHolder.set(authData)
    }

    private val newSslContext: SSLContext
        get() {
            val sslContext: SSLContext
            try {
                sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, null, null)
            } catch (e: GeneralSecurityException) {
                throw AssertionError() // The system has no TLS. Just give up.
            }
            return sslContext
        }

    //Network
    @Throws(Exception::class)
    override fun get(url: String): NetworkResponse {
        return request(NetworkRequest.Builder().url(url).build())
    }

    @Throws(Exception::class)
    override fun request(request: NetworkRequest): NetworkResponse {
        return request(request, this.client, null)
    }

    @Throws(Exception::class)
    override fun request(
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
                Log.d(
                    LOG_TAG, "Header $key : " + (if (privateHeaders.contains(
                            key
                        )
                    ) "private" else value)
                )
                requestBuilder.header(key, value)
            }
        }
        if (request.formHeaders != null || request.file != null) {
            Log.d(LOG_TAG, "Multipart " + request.isMultipartForm)
            if (request.formHeaders != null) {
                for ((key, value) in request.formHeaders) {
                    Log.d(
                        LOG_TAG, "Form header $key : " + (if (privateHeaders.contains(
                                key
                            )
                        ) "private" else value)
                    )
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
                    val type = MediaType.parse(file.mimeType)
                    val requestBody = RequestBodyUtil
                        .create(type, file.fileStream)
                    multipartBuilder.addFormDataPart(
                        file.requestName,
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
    fun request(
        request: NetworkRequest,
        client: OkHttpClient,
        uploadProgressListener: IWebClient.ProgressListener?
    ): NetworkResponse {
        val requestBuilder = prepareRequest(request, uploadProgressListener)
        val response = NetworkResponse(request.url)
        var okHttpResponse: Response? = null
        try {
            okHttpResponse = client.newCall(requestBuilder.build()).execute()
            if (!okHttpResponse.isSuccessful) {
                if (okHttpResponse.code() == 403) {
                    val content = okHttpResponse.body()!!.string()
                    //todo catch this is errorhandler
                    throw GoogleCaptchaException(content)
                }
                throw OkHttpResponseException(
                    okHttpResponse.code(),
                    okHttpResponse.message(),
                    request.url
                )
            }

            response.code = okHttpResponse.code()
            response.message = okHttpResponse.message()
            response.redirect = okHttpResponse.request().url().toString()

            if (!request.isWithoutBody) {
                response.body = okHttpResponse.body()!!.string()
                getCounts(response.body)
                checkForumErrors(response.body)
            }

            Log.d(
                LOG_TAG,
                "Response: $response"
            )
        } finally {
            okHttpResponse?.close()
        }
        return response
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

    override fun clearCookies() {
        clientCookies.clear()
    }

    companion object {
        private val LOG_TAG = Client::class.java.simpleName
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36"
    }
}
