package forpdateam.ru.forpda.model.data.storage

import java.io.InputStream

interface ExternalStorage {
    suspend fun getText(stream: InputStream): String
    suspend fun saveTextDefault(text: String, fileName: String)
}