package forpdateam.ru.forpda.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import io.appmetrica.analytics.AppMetrica
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by radiationx on 13.01.17.
 */
object FilePickHelper {
    private val LOG_TAG = FilePickHelper::class.java.simpleName

    fun pickFile(onlyImages: Boolean): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        if (onlyImages) {
            intent.setType("image/*")
        } else {
            intent.setType("*/*")
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return Intent.createChooser(intent, "Select file")
    }

    fun onActivityResult(context: Context, data: Intent): List<RequestFile> {
        val files: MutableList<RequestFile> = ArrayList()
        var tempFile: RequestFile?
        Log.d(LOG_TAG, "onActivityResult $data")
        if (data.data == null) {
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    tempFile = createFile(context, data.clipData!!.getItemAt(i).uri)
                    if (tempFile != null) files.add(tempFile)
                }
            }
        } else {
            tempFile = createFile(context, data.data!!)
            if (tempFile != null) files.add(tempFile)
        }
        return files
    }

    private fun createFile(context: Context, uri: Uri): RequestFile? {
        var requestFile: RequestFile? = null
        Log.d(LOG_TAG, "createFile $uri")
        try {
            var inputStream: InputStream? = null
            val name = getFileName(context, uri)
            val extension = MimeTypeUtil.getExtension(name)
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimeType == null) {
                mimeType = context.contentResolver.getType(uri)
            }
            if (mimeType == null) {
                mimeType = MimeTypeUtil.getType(extension)
            }
            if (uri.scheme == "content") {
                inputStream = context.contentResolver.openInputStream(uri)
            } else if (uri.scheme == "file") {
                inputStream = FileInputStream(File(uri.path))
            }
            checkNotNull(mimeType)
            checkNotNull(inputStream)
            requestFile = RequestFile(name, mimeType, inputStream)
        } catch (e: Exception) {
            AppMetrica.reportError(e.message!!, e)
        }
        return requestFile
    }

    private fun getFileName(context: Context, uri: Uri): String {
        Log.d(LOG_TAG, "getFileName " + uri.scheme + " : " + context.contentResolver.getType(uri))
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            Log.d(LOG_TAG, "res " + uri.path)
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result!!
    }
}
