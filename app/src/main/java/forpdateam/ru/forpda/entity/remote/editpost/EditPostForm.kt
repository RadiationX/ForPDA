package forpdateam.ru.forpda.entity.remote.editpost

/**
 * Created by radiationx on 10.01.17.
 */
// TODO refactor dis shiet
class EditPostForm {
    var type = TYPE_NEW_POST
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
    }

    fun fillFrom(post: EditPost) {
        postId = post.postId
        message = post.form.message
        post.form.editReason?.also {
            editReason = it
        }
        poll = post.poll?.let { createEditPoll(it) }
        post.attachments.forEach { attachment ->
            addAttachment(createAttachmentItem(attachment))
        }
    }

    private fun createAttachmentItem(attachment: EditPost.Attachment): AttachmentItem {
        val item = AttachmentItem()
        item.id = attachment.id
        item.name = attachment.name
        item.extension = attachment.extension
        item.weight = attachment.sizeFormatted
        item.md5 = attachment.md5
        when (attachment.type) {
            EditPost.Attachment.Type.File -> {
                item.typeFile = AttachmentItem.TYPE_FILE
            }

            is EditPost.Attachment.Type.Image -> {
                item.typeFile = AttachmentItem.TYPE_IMAGE
                item.imageUrl = attachment.type.url
                item.width = attachment.type.width
                item.height = attachment.type.height
            }
        }
        item.loadState = AttachmentItem.STATE_LOADED
        return item
    }

    private fun createEditPoll(poll: EditPost.Poll): EditPoll {
        val editPoll = EditPoll()
        editPoll.title = poll.title
        editPoll.maxQuestions = poll.maxQuestions
        editPoll.maxChoices = poll.maxChoices
        editPoll.baseIndexOffset = poll.baseIndexOffset
        poll.questions.forEach { question ->
            val editPollQuestion = EditPoll.Question()
            editPollQuestion.index = question.id.index
            editPollQuestion.title = question.title
            editPollQuestion.isMulti = question.isMulti
            editPollQuestion.baseIndexOffset = question.baseIndexOffset
            question.choices.forEach { choice ->
                val editPollChoice = EditPoll.Choice()
                editPollChoice.index = choice.id.index
                editPollChoice.title = choice.title
                editPollChoice.votes = choice.votes
                editPollQuestion.addChoice(editPollChoice)
            }
            editPoll.addQuestion(editPollQuestion)
        }
        return editPoll
    }
}

