package forpdateam.ru.forpda.model.data.remote.api.attachments

import android.content.Context
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import okio.HashingSource
import okio.blackholeSink
import okio.buffer
import okio.source
import javax.inject.Inject

class AttachmentsApi @Inject constructor(
    private val context: Context,
    private val webClient: IWebClient,
    private val attachmentsParser: AttachmentsParser
) {

    suspend fun uploadQmsFiles(files: List<RequestFile>, pending: List<AttachmentItem>) =
        uploadFiles(-1, "MSG", files, pending)

    suspend fun uploadTopicFiles(
        postId: Int,
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ) =
        uploadFiles(postId, null, files, pending)

    suspend fun deleteQmsFiles(items: List<AttachmentItem>) =
        deleteFiles(-1, "MSG", items)

    suspend fun deleteTopicFiles(postId: Int, items: List<AttachmentItem>) =
        deleteFiles(postId, null, items)


    private suspend fun uploadFiles(
        postId: Int,
        relType: String?,
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ): List<AttachmentItem> {
        files.indices.forEach {
            val file = files[it]
            val item = pending[it]
            if (getUploadedAttachment(postId, file, item) == null) {
                uploadAttachment(postId, relType, file, item)
            }
        }
        return pending
    }

    private suspend fun getUploadedAttachment(
        postId: Int,
        file: RequestFile,
        item: AttachmentItem
    ): AttachmentItem? {
        val metaData = file.getMetaData(context)
        val md5Hash = file.openInputStream(context).source().use { source ->
            val hashingSource = HashingSource.md5(source)
            hashingSource.buffer().use { bufferedSource ->
                bufferedSource.readAll(blackholeSink())
            }
            hashingSource.hash.hex()
        }

        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=attach")
            .xhrHeader()
            .formHeader("index", "1")
            .formHeader("maxSize", "134217728")
            .formHeader("allowExt", "")
            .formHeader("forum-attach-files", "")
            .formHeader("code", "check")
            .formHeader("md5", md5Hash)
            .formHeader("size", metaData.size.toString())
            .formHeader("name", metaData.name)
        if (postId != -1) {
            builder.formHeader("relId", postId.toString())
        }
        val response = webClient.request(builder.build())
        if (response.body == "0") {
            return null
        }
        return attachmentsParser.parseAttachment(response.body, item).also {
            it.status = AttachmentItem.STATUS_UPLOADED
        }
    }

    private suspend fun uploadAttachment(
        postId: Int,
        relType: String?,
        file: RequestFile,
        item: AttachmentItem
    ): AttachmentItem {
        val uploadRequest = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=attach")
            .xhrHeader()
            .formHeader("index", "1")
            .formHeader("maxSize", "134217728")
            .formHeader("allowExt", "")
            .formHeader("forum-attach-files", "")
            .formHeader("code", "upload")
            .file(NetworkRequest.File("FILE_UPLOAD[]", file, item.itemProgressListener))

        if (postId != -1) {
            uploadRequest.formHeader("relId", postId.toString())
        }
        if (relType != null) {
            uploadRequest.formHeader("relType", relType)
        }

        val response = webClient.request(uploadRequest.build())
        return attachmentsParser.parseAttachment(response.body, item).also {
            it.status = AttachmentItem.STATUS_UPLOADED
        }
    }

    private suspend fun deleteFiles(
        postId: Int,
        relType: String?,
        items: List<AttachmentItem>
    ): List<AttachmentItem> {
        var response: NetworkResponse
        for (item in items) {
            val builder = NetworkRequest.Builder()
                .url("https://4pda.to/forum/index.php?act=attach")
                .xhrHeader()
                .formHeader("index", "1")
                .formHeader("maxSize", "134217728")
                .formHeader("allowExt", "")
                .formHeader("code", "remove")
                .formHeader("id", Integer.toString(item.id))
            if (postId != -1) {
                builder.formHeader("relId", postId.toString())
            }
            if (relType != null) {
                builder.formHeader("relType", relType)
            }
            response = webClient.request(builder.build())
            //todo проверка на ошибки, я хз че еще может быть кроме 0
            if (response.body == "0") {
                item.status = AttachmentItem.STATUS_REMOVED
                item.isError = false
            }
        }
        return items
    }
}