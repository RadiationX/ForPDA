package forpdateam.ru.forpda.model.system

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import forpdateam.ru.forpda.model.data.storage.ExternalStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import java.io.File
import java.io.InputStream

class ExternalStorage(
    private val context: Context,
    private val permissionsController: MintPermissionsController
) : ExternalStorageProvider {

    override suspend fun getText(stream: InputStream): String {
        return withContext(Dispatchers.IO) {
            stream.bufferedReader().use {
                it.readText()
            }
        }
    }

    override suspend fun saveTextDefault(text: String, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveWithContextResolver(text, fileName)
        }

        if (permissionsController.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).isGranted()) {
            saveTextToFile(text, fileName)
            return
        }

        throw Exception("File not saved")
    }

    private suspend fun saveTextToFile(text: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
            val directory = File(root)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(root, fileName)
            if (file.exists()) {
                file.delete()
            }
            file.outputStream().bufferedWriter().use {
                it.append(text)
            }
            file.absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWithContextResolver(text: String, fileName: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = requireNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)) {
            "uri is null"
        }

        resolver.openOutputStream(uri)?.use { outputStream ->
            text.byteInputStream().copyTo(outputStream)
        }
    }
}