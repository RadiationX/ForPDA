package forpdateam.ru.forpda.model.data.storage

import java.io.InputStream

interface ExternalStorageProvider {
    suspend fun getText(stream: InputStream): String
    suspend fun saveTextDefault(text: String, fileName: String): String
    suspend fun saveText(text: String, fileName: String, path: String): String
}