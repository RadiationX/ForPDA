package forpdateam.ru.forpda.model.data.remote.api.attachments

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import java.io.ByteArrayInputStream
import java.security.MessageDigest

class AttachmentsApi(
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

        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=attach")
            .xhrHeader()
            .formHeader("index", "1")
            .formHeader("maxSize", "134217728")
            .formHeader("allowExt", "")
            .formHeader("forum-attach-files", "")
            .formHeader("code", "check")
        if (postId != -1) {
            builder.formHeader("relId", postId.toString())
        }
        for (i in files.indices) {
            val file = files[i]
            val item = pending[i]

            file.requestName = "FILE_UPLOAD[]"
            val messageDigest = MessageDigest.getInstance("MD5")
            file.fileStream = file.fileStream.use {
                val targetArray = ByteArray(it.available()).apply {
                    it.read(this)
                }
                messageDigest.update(targetArray)
                ByteArrayInputStream(targetArray)
            }
            val hash = messageDigest.digest()
            val md5 = byteArrayToHexString(hash)
            builder
                .formHeader("md5", md5)
                .formHeader("size", file.fileStream.available().toString())
                .formHeader("name", file.fileName)

            var response = webClient.request(builder.build())
            if (response.body == "0") {
                val uploadRequest = NetworkRequest.Builder()
                    .url("https://4pda.to/forum/index.php?act=attach")
                    .xhrHeader()
                    .formHeader("index", "1")
                    .formHeader("maxSize", "134217728")
                    .formHeader("allowExt", "")
                    .formHeader("forum-attach-files", "")
                    .formHeader("code", "upload")
                    .file(file)

                if (postId != -1) {
                    uploadRequest.formHeader("relId", postId.toString())
                }
                if (relType != null) {
                    uploadRequest.formHeader("relType", relType)
                }

                response = webClient.request(uploadRequest.build(), item.itemProgressListener)
            }
            attachmentsParser.parseAttachment(response.body, item)
            item.status = AttachmentItem.STATUS_UPLOADED
        }
        return pending
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

    private fun byteArrayToHexString(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (aByte in bytes) {
            val hex = Integer.toHexString(aByte.toInt() and 0xFF)
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}