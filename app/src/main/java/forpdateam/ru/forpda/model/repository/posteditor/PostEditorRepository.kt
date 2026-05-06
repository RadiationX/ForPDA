package forpdateam.ru.forpda.model.repository.posteditor

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi

/**
 * Created by radiationx on 01.01.18.
 */

class PostEditorRepository(
    private val editPostApi: EditPostApi,
    private val attachmentsApi: AttachmentsApi,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun loadForm(postId: Int): EditPost {
        return editPostApi.loadForm(postId)
    }

    suspend fun uploadFiles(
        id: Int,
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ): List<AttachmentItem> {
        return attachmentsApi.uploadTopicFiles(id, files, pending)
    }

    suspend fun deleteFiles(id: Int, items: List<AttachmentItem>): List<AttachmentItem> {
        return attachmentsApi.deleteTopicFiles(id, items)
    }

    suspend fun sendPost(form: EditPostForm): ThemePage {
        return editPostApi.sendPost(form).also {
            saveUsers(it)
        }
    }

    private suspend fun saveUsers(page: ThemePage) {
        val forumUsers = page.posts.map { post ->
            post.post.user
        }
        forumUsersCache.saveUsers(forumUsers)
    }

}
