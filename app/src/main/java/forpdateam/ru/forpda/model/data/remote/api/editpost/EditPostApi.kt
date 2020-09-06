package forpdateam.ru.forpda.model.data.remote.api.editpost

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsParser
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeParser
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.*

/**
 * Created by radiationx on 10.01.17.
 */

class EditPostApi(
        private val webClient: IWebClient,
        private val themeApi: ThemeApi,
        private val editPostParser: EditPostParser,
        private val attachmentsParser: AttachmentsParser,
        private val themeParser: ThemeParser
) {

    fun loadForm(postId: Int): EditPostForm {
        val url = "https://4pda.ru/forum/index.php?act=post&do=edit_post&p=" + Integer.toString(postId)
        var response = webClient.get(url)
        if (response.body == "nopermission") {
            return EditPostForm().apply {
                errorCode = EditPostForm.ERROR_NO_PERMISSION
            }
        }

        val form = editPostParser.parseForm(response.body)
        form.poll = editPostParser.parsePoll(response.body)

        response = webClient.get("https://4pda.ru/forum/index.php?act=attach&index=1&relId=$postId&maxSize=134217728&allowExt=&code=init&unlinked=")
        val attachments = attachmentsParser.parseAttachments(response.body)
        form.attachments.addAll(attachments)

        return form
    }

    fun sendPost(form: EditPostForm): ThemePage {
        val url = "https://4pda.ru/forum/index.php"
        val headers = HashMap<String, String>()

        val builder = NetworkRequest.Builder()
                .url(url)
                .formHeaders(headers)
                .multipart()
                .formHeader("act", "Post")
                .formHeader("CODE", if (form.type == EditPostForm.TYPE_NEW_POST) "03" else "9")
                .formHeader("f", form.forumId.toString())
                .formHeader("t", form.topicId.toString())
                .formHeader("auth_key", webClient.authKey)
                .formHeader("Post", form.message)
                .formHeader("enablesig", "yes")
                .formHeader("enableemo", "yes")
                .formHeader("st", form.st.toString())
                .formHeader("removeattachid", "0")
                .formHeader("MAX_FILE_SIZE", "0")
                .formHeader("parent_id", "0")
                .formHeader("ed-0_wysiwyg_used", "0")
                .formHeader("editor_ids[]", "ed-0")
                .formHeader("iconid", "0")
                .formHeader("_upload_single_file", "1")

        val poll = form.poll
        if (poll != null) {
            builder.formHeader("poll_question", poll.title.replace("\n".toRegex(), " "))
            for (i in 0 until poll.questions.size) {
                val question = poll.getQuestion(i)
                val q_index = i + 1
                builder.formHeader("question[$q_index]", question.title.replace("\n".toRegex(), " "))
                builder.formHeader("multi[$q_index]", if (question.isMulti) "1" else "0")
                for (j in 0 until question.choices.size) {
                    val choice = question.getChoice(j)
                    val c_index = j + 1
                    builder.formHeader("choice[$q_index${'_'}$c_index]", choice.title.replace("\n".toRegex(), " "))
                }
            }
        }

        //.formHeader("file-list", addedFileList);
        if (form.type == EditPostForm.TYPE_EDIT_POST) {
            builder.formHeader("post_edit_reason", form.editReason)
        }
        val ids = StringBuilder()
        if (form.attachments != null && !form.attachments.isEmpty()) {
            for (i in 0 until form.attachments.size) {
                val id = form.attachments[i].id
                ids.append(id)
                if (i < form.attachments.size - 1) {
                    ids.append(",")
                }
            }
        }
        builder.formHeader("file-list", ids.toString())
        if (form.postId != 0)
            builder.formHeader("p", form.postId.toString())

        val response = webClient.request(builder.build())
        val redirectUrl = response.redirect ?: url
        return themeParser.parsePage(response.body, redirectUrl, false, false)
    }

}
