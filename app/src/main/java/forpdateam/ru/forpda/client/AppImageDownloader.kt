package forpdateam.ru.forpda.client

import android.content.Context
import android.util.Log
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import forpdateam.ru.forpda.common.di.ImagesOkHttpClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import javax.inject.Inject

class AppImageDownloader @Inject constructor(
    context: Context,
    @param:ImagesOkHttpClient private val okHttpClient: OkHttpClient
) : BaseImageDownloader(context) {

    override fun getStream(imageUri: String, extra: Any?): InputStream {
        var imageUri = imageUri
        if (imageUri.startsWith("//")) {
            imageUri = "http:$imageUri"
        }
        Log.d(AppImageDownloader::class.java.simpleName, "ImageLoader getStream $imageUri")
        return super.getStream(imageUri, extra)
    }

    override fun getStreamFromNetwork(imageUri: String, extra: Any?): InputStream {
        val request = Request.Builder().get().url(imageUri).build()
        val response = okHttpClient.newCall(request).execute()
        return response.body.byteStream()
    }
}