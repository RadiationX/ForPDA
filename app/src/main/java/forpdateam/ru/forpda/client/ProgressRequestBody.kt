package forpdateam.ru.forpda.client

import forpdateam.ru.forpda.model.data.remote.IWebClient
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import okio.Sink
import java.io.IOException

/**
 * Created by radiationx on 10.11.17.
 */
class ProgressRequestBody internal constructor(
    private val mDelegate: RequestBody,
    private val mListener: IWebClient.ProgressListener
) :
    RequestBody() {
    override fun contentType(): MediaType? {
        return mDelegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return mDelegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val mCountingSink: CountingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(mCountingSink)
        mDelegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            mListener.onProgress((100f * bytesWritten / contentLength()).toInt())
        }
    }
}
