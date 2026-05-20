package forpdateam.ru.forpda.model.data.remote.api.editpost

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.editpost.EditPostPermissionException
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsParser
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeParser
import javax.inject.Inject

/**
 * Created by radiationx on 10.01.17.
 */

class EditPostApi @Inject constructor(
    private val webClient: WebClient,
    private val editPostParser: EditPostParser,
    private val attachmentsParser: AttachmentsParser,
    private val themeParser: ThemeParser,
    private val authHolder: AuthHolder
) {

    suspend fun loadForm(postId: Int): EditPost {
        val postResponse = webClient.request(ApiRequest.Forum.Post.Edit(postId))
        if (postResponse.body == "nopermission") {
            throw EditPostPermissionException()
        }

        val attachmentsResponse = webClient.request(ApiRequest.Forum.Attachments.GetAttachedToPost(postId))
        val form = editPostParser.parseForm(postResponse.body)
        val poll = editPostParser.parsePoll(postResponse.body)
        val attachments = attachmentsParser.parseAttachments(attachmentsResponse.body)

        return EditPost(
            postId = postId,
            form = form,
            poll = poll,
            attachments = attachments
        )
    }

    suspend fun sendPost(form: EditPostForm): ThemePage {
        val response = webClient.request(ApiRequest.Forum.Post.Send(form, authHolder.getAuthKey()))
        val redirectUrl = response.redirect
        return themeParser.parsePage(response.body, redirectUrl, false, false)
    }

}
