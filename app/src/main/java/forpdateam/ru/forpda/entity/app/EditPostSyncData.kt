package forpdateam.ru.forpda.entity.app

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem

data class EditPostSyncData(
    val topicId: Int,
    val message: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val attachments: List<AttachmentItem>
)