package forpdateam.ru.forpda.model.data.remote.api.attachments

import android.content.Context
import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.model.data.remote.WebClient
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
    private val webClient: WebClient,
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
        val response = webClient.request(ApiRequest.Forum.Attachments.GetExisted(postId, md5Hash, metaData.size, metaData.name))
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
        val networkFile = NetworkRequest.File(file, item.itemProgressListener)
        val response = webClient.request(ApiRequest.Forum.Attachments.Upload(postId, relType, networkFile))
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
            response = webClient.request(ApiRequest.Forum.Attachments.Delete(item.id, postId, relType))
            //todo проверка на ошибки, я хз че еще может быть кроме 0
            if (response.body == "0") {
                item.status = AttachmentItem.STATUS_REMOVED
                item.isError = false
            }
        }
        return items
    }
}