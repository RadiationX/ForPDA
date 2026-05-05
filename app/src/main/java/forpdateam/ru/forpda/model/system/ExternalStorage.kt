package forpdateam.ru.forpda.model.system

import android.os.Environment
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class ExternalStorage : ExternalStorageProvider {

    override suspend fun getText(stream: InputStream): String {
        return withContext(Dispatchers.IO) {
            stream.bufferedReader().use {
                it.readText()
            }
        }
    }

    override suspend fun saveText(text: String, fileName: String, path: String): String {
        return withContext(Dispatchers.IO) {
            val directory = File(path)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(path, fileName)
            if (file.exists()) {
                file.delete()
            }
            file.outputStream().bufferedWriter().use {
                it.append(text)
            }
            file.absolutePath
        }
    }

    override suspend fun saveTextDefault(text: String, fileName: String): String {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .toString()
        return saveText(text, fileName, root)
    }
}