package forpdateam.ru.forpda.common.webview

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App.Companion.get
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.regex.Pattern

/**
 * Created by radiationx on 12.09.17.
 */
open class CustomWebViewClient : WebViewClient() {
    private val cachePattern: Pattern =
        Pattern.compile("app_cache:avatars\\?(url|nick)=([\\s\\S]*)")

    private val avatarRepository = get().Di().avatarRepository
    private val linkHandler = get().Di().linkHandler

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val matcher = cachePattern.matcher(url)
        if (matcher.find()) {
            try {
                Log.d(LOG_TAG, "intercepted $url")
                var resourceResponse: WebResourceResponse? = null
                val type = matcher.group(1)
                var value = matcher.group(2)
                value = URLDecoder.decode(value, "UTF-8")

                var avatarUrl: String? = null
                when (type) {
                    TYPE_NICK -> avatarUrl = avatarRepository.getAvatarSync(value)
                    TYPE_URL -> avatarUrl = value
                }
                Log.d(
                    "lalala",
                    "shouldInterceptRequest: avatar: $avatarUrl : value: $value"
                )

                val bitmap = ImageLoader.getInstance().loadImageSync(avatarUrl)
                var base64Bitmap = convert(bitmap)
                base64Bitmap = "data:image/png;base64,$base64Bitmap"
                resourceResponse = WebResourceResponse(
                    "text/text",
                    null,
                    ByteArrayInputStream(base64Bitmap.toByteArray())
                )
                return resourceResponse
            } catch (e: Exception) {
                e.printStackTrace()
                super.shouldInterceptRequest(view, url)
            }
        }
        return super.shouldInterceptRequest(view, url)
    }

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            Base64.DEFAULT
        )

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun convert(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    @Suppress("deprecation")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return handleUri(Uri.parse(url))
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return handleUri(request.url)
    }

    open fun handleUri(uri: Uri): Boolean {
        linkHandler.handle(uri.toString(), null)
        return true
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handler.proceed()
        } else {
            super.onReceivedSslError(view, handler, error)
        }
    }

    companion object {
        private val LOG_TAG = CustomWebViewClient::class.java.simpleName
        private const val TYPE_NICK = "nick"
        private const val TYPE_URL = "url"
    }
}
