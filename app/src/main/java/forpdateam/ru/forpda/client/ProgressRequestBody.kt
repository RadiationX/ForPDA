package forpdateam.ru.forpda.client

import forpdateam.ru.forpda.model.data.remote.IWebClient
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer

/**
 * Created by radiationx on 10.11.17.
 */
class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val listener: IWebClient.ProgressListener
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(sink: Sink) : ForwardingSink(sink) {
        private var bytesWritten: Long = 0

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener.onProgress((100f * bytesWritten / contentLength()).toInt())
        }
    }
}
