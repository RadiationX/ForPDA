package forpdateam.ru.forpda.model.data.remote.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import forpdateam.ru.forpda.common.MimeTypeUtil
import java.io.InputStream

/**
 * Created by radiationx on 12.01.17.
 */
data class RequestFile(
    val uri: Uri,
) {

    fun getMetaData(context: Context): MetaData {
        return context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            requireNotNull(cursor) {
                "Can't query for $uri"
            }
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            require(cursor.moveToFirst()) {
                "Can't find metadata by cursor for $uri"
            }
            val fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)
            val mimeType = getMimeType(context, uri, fileName)
            MetaData(
                name = fileName,
                size = fileSize,
                mimeType = mimeType
            )
        }
    }

    fun openInputStream(context: Context): InputStream {
        return requireNotNull(context.contentResolver.openInputStream(uri)) {
            "InputStream is null for '$uri'"
        }
    }

    private fun getMimeType(context: Context, uri: Uri, fileName: String): String {
        val extension = MimeTypeUtil.getExtension(fileName)
        val mimeType = context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: MimeTypeUtil.getType(extension)
        return requireNotNull(mimeType) {
            "MimeType is null for '$uri'"
        }
    }

    data class MetaData(
        val name: String,
        val size: Long,
        val mimeType: String
    )
}
