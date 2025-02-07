package forpdateam.ru.forpda.model.data.remote.api

import java.io.InputStream

/**
 * Created by radiationx on 12.01.17.
 */
class RequestFile(
    val fileName: String,
    val mimeType: String,
    var fileStream: InputStream
) {
    var requestName: String? = null

    override fun toString(): String {
        return "RequestFile{$fileName, $mimeType, $requestName, $fileStream}"
    }
}
