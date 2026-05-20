package forpdateam.ru.forpda.client

import android.content.Context
import android.util.Log
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request

class NetworkRequestMapper(
    private val context: Context
) {
    companion object {
        private val LOG_TAG = NetworkRequestMapper::class.java.simpleName
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36"
    }

    fun map(request: NetworkRequest): Request {
        Log.d(LOG_TAG, "Request url " + request.url)
        request.headers.onEach { (key, value) ->
            Log.d(LOG_TAG, "Header $key : ${getPrivateHeaderValue(key, value)}")
        }
        Log.d(LOG_TAG, "Multipart " + request.isMultipartForm)
        request.formHeaders.onEach { (key, value) ->
            Log.d(LOG_TAG, "Form header $key : ${getPrivateHeaderValue(key, value)}")
        }
        request.files.onEach { (key, file) ->
            Log.d(LOG_TAG, "Form file $key: $file")
        }
        var url = request.url
        if (request.url.startsWith("//")) {
            url = "https:" + request.url
        }
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
            .header("User-Agent", USER_AGENT)

        if (request.isWithoutBody) {
            requestBuilder.head()
        }

        request.headers.onEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        if (request.isMultipartForm) {
            fillMultipartBody(request, requestBuilder)
        } else {
            fillFormBody(request, requestBuilder)
        }
        return requestBuilder.build()
    }

    private fun fillFormBody(request: NetworkRequest, requestBuilder: Request.Builder) {
        if (request.formHeaders.isEmpty()) {
            return
        }
        val formBuilder = FormBody.Builder()
        request.formHeaders.onEach { (key, value) ->
            formBuilder.add(key, value)
        }
        requestBuilder.post(formBuilder.build())
    }

    private fun fillMultipartBody(
        request: NetworkRequest,
        requestBuilder: Request.Builder
    ) {
        if (request.formHeaders.isEmpty() && request.files.isEmpty()) {
            return
        }
        val multipartBuilder = MultipartBody.Builder()
        multipartBuilder.setType(MultipartBody.FORM)
        request.formHeaders.onEach { (key, value) ->
            multipartBuilder.addFormDataPart(key, value)
        }
        request.files.onEach { (key, file) ->
            val metaData = file.file.getMetaData(context)
            val type = metaData.mimeType.toMediaTypeOrNull()
            val requestBody = InputStreamRequestBody(type, file.file.openInputStream(context)).let {
                ProgressRequestBody(it, file.progressListener)
            }
            multipartBuilder.addFormDataPart(
                name = key,
                filename = metaData.name,
                body = requestBody
            )
        }
        requestBuilder.post(multipartBuilder.build())
    }


    private fun getPrivateHeaderValue(key: String, value: String): String {
        return if (key in CookieStorage.AUTH_COOKIES) {
            "private"
        } else {
            value
        }
    }
}