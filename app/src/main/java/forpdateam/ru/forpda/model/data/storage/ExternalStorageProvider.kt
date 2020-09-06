package forpdateam.ru.forpda.model.data.storage

import java.io.InputStream

interface ExternalStorageProvider {
    fun getText(stream: InputStream): String
    fun saveTextDefault(text: String, fileName: String): String
    fun saveText(text: String, fileName: String, path: String): String
}