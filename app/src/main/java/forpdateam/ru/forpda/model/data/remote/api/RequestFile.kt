package forpdateam.ru.forpda.model.data.remote.api

import java.io.InputStream

/**
 * Created by radiationx on 12.01.17.
 */
class RequestFile {
    var fileStream: InputStream
    val fileName: String
    val mimeType: String
    var requestName: String? = null

    constructor(fileName: String, mimeType: String, fileStream: InputStream) {
        this.fileStream = fileStream
        this.fileName = fileName
        this.mimeType = mimeType
    }

    constructor(requestName: String?, fileName: String, mimeType: String, fileStream: InputStream) {
        this.requestName = requestName
        this.fileStream = fileStream
        this.fileName = fileName
        this.mimeType = mimeType
    }

    override fun toString(): String {
        return "RequestFile{$fileName, $mimeType, $requestName, $fileStream}"
    }
}
