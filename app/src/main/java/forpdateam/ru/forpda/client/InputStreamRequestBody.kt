package forpdateam.ru.forpda.client

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.InputStream

class InputStreamRequestBody(
    private val contentType: MediaType?,
    private val inputStream: InputStream
) : RequestBody() {

    override fun contentType() = contentType

    override fun contentLength(): Long {
        val available = inputStream.available().toLong()
        return if (available != 0L) {
            available
        } else {
            -1L
        }
    }

    override fun writeTo(sink: BufferedSink) {
        inputStream.source().use { source ->
            sink.writeAll(source)
        }
    }
}
