package forpdateam.ru.forpda.common.webview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.presentation.LinkHandler
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 12.09.17.
 */
open class CustomWebViewClient @Inject constructor(
    private val avatarRepository: AvatarRepository,
    private val linkHandler: LinkHandler
) : WebViewClient() {
    private val cachePattern: Pattern =
        Pattern.compile("app_cache:avatars\\?(url|nick)=([\\s\\S]*)")

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val matcher = cachePattern.matcher(url)
        if (matcher.find()) {
            try {
                Log.d(LOG_TAG, "intercepted $url")
                var resourceResponse: WebResourceResponse? = null
                val type = matcher.group(1)
                var value = matcher.group(2)
                value = URLDecoder.decode(value, "UTF-8")
                val avatarUrl = when (type) {
                    TYPE_NICK -> coRunCatching {
                        runBlocking {
                            avatarRepository.getAvatar(value)
                        }
                    }.getOrNull()

                    TYPE_URL -> value
                    else -> null
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

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return handleUri(url.toUri())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return handleUri(request.url)
    }

    open fun handleUri(uri: Uri): Boolean {
        linkHandler.handle(uri.toString())
        return true
    }

    companion object {
        private val LOG_TAG = CustomWebViewClient::class.java.simpleName
        private const val TYPE_NICK = "nick"
        private const val TYPE_URL = "url"
    }
}
