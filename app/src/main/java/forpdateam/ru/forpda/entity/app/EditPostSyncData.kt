package forpdateam.ru.forpda.entity.app

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem

class EditPostSyncData {
    var topicId: Int = 0
    var message: String? = null
    var selectionStart = 0
    var selectionEnd = 0;
    var attachments: List<AttachmentItem>? = null
}