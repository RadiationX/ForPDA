package forpdateam.ru.forpda.model.repository.posteditor

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.editpost.EditPostApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class PostEditorRepository(
        private val schedulers: SchedulersProvider,
        private val editPostApi: EditPostApi,
        private val attachmentsApi: AttachmentsApi,
        private val forumUsersCache: ForumUsersCache
) : BaseRepository(schedulers) {

    fun loadForm(postId: Int): Single<EditPostForm> = Single
            .fromCallable { editPostApi.loadForm(postId) }
            .runInIoToUi()

    fun uploadFiles(id: Int, files: List<RequestFile>, pending: List<AttachmentItem>): Single<List<AttachmentItem>> = Single
            .fromCallable { attachmentsApi.uploadTopicFiles(id, files, pending) }
            .runInIoToUi()

    fun deleteFiles(id: Int, items: List<AttachmentItem>): Single<List<AttachmentItem>> = Single
            .fromCallable { attachmentsApi.deleteTopicFiles(id, items) }
            .runInIoToUi()

    fun sendPost(form: EditPostForm): Single<ThemePage> = Single
            .fromCallable { editPostApi.sendPost(form) }
            .doOnSuccess { saveUsers(it) }
            .runInIoToUi()

    private fun saveUsers(page: ThemePage) {
        val forumUsers = page.posts.map { post ->
            ForumUser().apply {
                id = post.userId
                nick = post.nick
                avatar = post.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }

}
