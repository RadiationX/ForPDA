package forpdateam.ru.forpda.model.system

import android.os.Environment
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import java.io.*

class ExternalStorage : ExternalStorageProvider {

    override fun getText(stream: InputStream): String = stream.bufferedReader().use {
        it.readText()
    }

    override fun saveText(text: String, fileName: String, path: String): String {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(path, fileName)
        if (file.exists()) {
            file.delete()
        }
        FileOutputStream(file).use {
            OutputStreamWriter(it).use {
                it.append(text)
            }
        }
        return file.absolutePath
    }

    override fun saveTextDefault(text: String, fileName: String): String {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
        return saveText(text, fileName, root)
    }
}