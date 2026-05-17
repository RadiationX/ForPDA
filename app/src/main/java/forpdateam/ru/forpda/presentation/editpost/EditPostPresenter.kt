package forpdateam.ru.forpda.presentation.editpost

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.editpost.EditPostPermissionException
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.theme.ThemeTemplate
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class EditPostPresenter(
    private val editorRepository: PostEditorRepository,
    private val themeTemplate: ThemeTemplate,
    private val router: TabRouter,
    private val errorHandler: ErrorHandler
) : BasePresenter<EditPostView>() {

    private val postForm = EditPostForm()

    fun initPostForm(newPostForm: EditPostForm) {
        postForm.apply {
            postForm.type = newPostForm.type
            postForm.attachments.addAll(newPostForm.attachments)
            postForm.message = newPostForm.message
            postForm.forumId = newPostForm.forumId
            postForm.topicId = newPostForm.topicId
            postForm.postId = newPostForm.postId
            postForm.st = newPostForm.st
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (postForm.type == EditPostForm.TYPE_EDIT_POST) {
            loadForm()
        } else {
            viewState.showForm(postForm)
        }
    }

    fun sendMessage(message: String, attachments: List<AttachmentItem>) {
        postForm.message = message
        postForm.attachments.clear()
        for (item in attachments) {
            postForm.addAttachment(item)
        }
        viewModelScope.launch {
            coRunCatching {
                editorRepository.sendPost(postForm)
            }.map {
                themeTemplate.mapEntity(it)
            }.onSuccess {
                viewState.onPostSend(it, postForm)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun loadForm() {
        viewModelScope.launch {
            coRunCatching {
                editorRepository.loadForm(postForm.postId)
            }.onSuccess {
                postForm.fillFrom(it)
                viewState.showForm(postForm)
            }.onFailure {
                if (it is EditPostPermissionException) {
                    viewState.onNoPermission()
                } else {
                    errorHandler.handle(it)
                }
            }
        }
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                editorRepository.uploadFiles(postForm.postId, files, pending)
            }.onSuccess {
                viewState.onUploadFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun deleteFiles(items: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                editorRepository.deleteFiles(postForm.postId, items)
            }.onSuccess {
                viewState.onDeleteFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onSendClick() {
        if (postForm.type == EditPostForm.TYPE_EDIT_POST) {
            viewState.showReasonDialog(postForm)
        } else {
            viewState.sendMessage()
        }
    }

    fun onReasonEdit(reason: String) {
        postForm.editReason = reason
        viewState.sendMessage()
    }

    fun exit() {
        router.exit()
    }

    fun exitWithSync(message: String, intArray: IntArray, attachments: List<AttachmentItem>) {
        router.sendResult(
            Screen.Theme.CODE_RESULT_SYNC, EditPostSyncData(
                topicId = postForm.topicId,
                message = message,
                selectionStart = intArray[0],
                selectionEnd = intArray[1],
                attachments = attachments,
            )
        )
    }

    fun exitWithPage(page: ThemePage) {
        router.sendResult(Screen.Theme.CODE_RESULT_PAGE, page)
    }
}
