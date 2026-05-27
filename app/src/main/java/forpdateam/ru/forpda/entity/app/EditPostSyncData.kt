package forpdateam.ru.forpda.entity.app

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import ru.radiationx.coretypes.TopicId

data class EditPostSyncData(
    val topicId: TopicId,
    val message: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val attachments: List<AttachmentItem>
)