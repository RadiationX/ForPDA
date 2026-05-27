package forpdateam.ru.forpda.model.repository.posteditor

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import ru.radiationx.coretypes.PostId
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class PostEditorRepository @Inject constructor(
    private val editPostApi: EditPostApi,
    private val attachmentsApi: AttachmentsApi,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun loadForm(postId: PostId): EditPost {
        return editPostApi.loadForm(postId)
    }

    suspend fun uploadFiles(postId: PostId?, files: List<RequestFile>, pending: List<AttachmentItem>): List<AttachmentItem> {
        return attachmentsApi.uploadTopicFiles(postId, files, pending)
    }

    suspend fun deleteFiles(postId: PostId?, items: List<AttachmentItem>): List<AttachmentItem> {
        return attachmentsApi.deleteTopicFiles(postId, items)
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
        forumUsersCache.savePostUsers(forumUsers)
    }

}
