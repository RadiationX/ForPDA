package forpdateam.ru.forpda.entity.remote.editpost

import java.util.ArrayList

/**
 * Created by radiationx on 10.01.17.
 */

class EditPostForm {
    var type = TYPE_NEW_POST
    var errorCode = 0
    val attachments = ArrayList<AttachmentItem>()
    var editReason = "default_edit_reason"
    var message = ""
    var poll: EditPoll? = null

    var forumId = 0
    var topicId = 0
    var postId = 0
    var st = 0

    fun addAttachment(item: AttachmentItem) {
        attachments.add(item)
    }

    companion object {
        val ARG_TYPE = "type"
        val TYPE_NEW_POST = 0
        val TYPE_EDIT_POST = 1
        val ERROR_NONE = 0
        val ERROR_NO_PERMISSION = 1
    }
}
