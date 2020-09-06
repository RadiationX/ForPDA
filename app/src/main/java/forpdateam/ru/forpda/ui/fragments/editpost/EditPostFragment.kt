package forpdateam.ru.forpda.ui.fragments.editpost

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.FilePickHelper
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.presentation.editpost.EditPostPresenter
import forpdateam.ru.forpda.presentation.editpost.EditPostView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup

/**
 * Created by radiationx on 14.01.17.
 */

class EditPostFragment : TabFragment(), EditPostView {

    private var formType = 0

    private lateinit var messagePanel: MessagePanel
    private lateinit var attachmentsPopup: AttachmentsPopup
    private var pollPopup: EditPollPopup? = null

    @InjectPresenter
    lateinit var presenter: EditPostPresenter

    @ProvidePresenter
    fun providePresenter(): EditPostPresenter = EditPostPresenter(
            App.get().Di().editPostRepository,
            App.get().Di().themeTemplate,
            App.get().Di().router,
            App.get().Di().errorHandler
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            val postForm = EditPostForm()
            postForm.type = getInt(EditPostForm.ARG_TYPE)
            formType = postForm.type
            getParcelableArrayList<AttachmentItem>(ARG_ATTACHMENTS)?.also {
                postForm.attachments.addAll(it)
            }
            postForm.message = getString(ARG_MESSAGE, "")
            postForm.forumId = getInt(ARG_FORUM_ID)
            postForm.topicId = getInt(ARG_TOPIC_ID)
            postForm.postId = getInt(ARG_POST_ID)
            postForm.st = getInt(ARG_ST)
            presenter.initPostForm(postForm)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        messagePanel = MessagePanel(context, fragmentContainer, fragmentContent, true)
        attachmentsPopup = messagePanel.attachmentsPopup
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messagePanel.addSendOnClickListener { presenter.onSendClick() }
        attachmentsPopup.setAddOnClickListener { tryPickFile() }
        attachmentsPopup.setDeleteOnClickListener { removeFiles() }
        arguments?.apply {
            val title = getString(ARG_THEME_NAME, "")
            setTitle("${App.get().getString(if (formType == EditPostForm.TYPE_NEW_POST) R.string.editpost_title_answer else R.string.editpost_title_edit)} $title")
        }

        messagePanel.editPollButton.setOnClickListener {
            pollPopup?.show()
        }
    }

    override fun onResumeOrShow() {
        super.onResumeOrShow()
        messagePanel.onResume()
    }

    override fun onPauseOrHide() {
        super.onPauseOrHide()
        messagePanel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagePanel.onDestroy()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        messagePanel.hidePopupWindows()
    }

    override fun onBackPressed(): Boolean {
        super.onBackPressed()
        if (messagePanel.onBackPressed())
            return true

        if (showExitDialog()) {
            return true
        }

        //Синхронизация с полем в фрагменте темы
        if (formType == EditPostForm.TYPE_NEW_POST) {
            showSyncDialog()
            return true
        }
        return false
    }

    private fun tryPickFile() {
        App.get().checkStoragePermission({ startActivityForResult(FilePickHelper.pickFile(false), TabFragment.REQUEST_PICK_FILE) }, App.getActivity())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TabFragment.REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return
            }
            uploadFiles(FilePickHelper.onActivityResult(context, data))
        }
    }

    override fun showForm(form: EditPostForm) {
        if (form.errorCode != EditPostForm.ERROR_NONE) {
            Toast.makeText(context, R.string.editpost_error_edit, Toast.LENGTH_SHORT).show()
            presenter.exit()
            return
        }

        if (form.poll != null) {
            pollPopup = EditPollPopup(context)
            pollPopup?.setPoll(form.poll)
            messagePanel.editPollButton.visibility = View.VISIBLE
        } else {
            messagePanel.editPollButton.visibility = View.GONE
        }

        attachmentsPopup.onLoadAttachments(form)
        messagePanel.insertText(form.message)
        messagePanel.messageField.requestFocus()
        showKeyboard(messagePanel.messageField)
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        messagePanel.formProgress.visibility = if (isRefreshing) View.VISIBLE else View.GONE
        messagePanel.messageField.visibility = if (isRefreshing) View.GONE else View.VISIBLE
        messagePanel.formProgress.visibility = View.GONE
    }

    override fun setSendRefreshing(isRefreshing: Boolean) {
        messagePanel.setProgressState(isRefreshing)
    }

    override fun sendMessage() {
        presenter.sendMessage(messagePanel.message, messagePanel.attachments)
    }

    override fun onPostSend(page: ThemePage, form: EditPostForm) {
        presenter.exitWithPage(page)
    }


    fun uploadFiles(files: List<RequestFile>) {
        val pending = attachmentsPopup.preUploadFiles(files)
        presenter.uploadFiles(files, pending)
    }

    private fun removeFiles() {
        attachmentsPopup.preDeleteFiles()
        val selectedFiles = attachmentsPopup.getSelected()
        presenter.deleteFiles(selectedFiles)
    }

    override fun onUploadFiles(items: List<AttachmentItem>) {
        attachmentsPopup.onUploadFiles(items)
    }

    override fun onDeleteFiles(items: List<AttachmentItem>) {
        attachmentsPopup.onDeleteFiles(items)
    }


    override fun showReasonDialog(form: EditPostForm) {
        val view = View.inflate(context, R.layout.edit_post_reason, null)
        val editText = view.findViewById<View>(R.id.edit_post_reason_field) as EditText
        editText.setText(form.editReason)

        AlertDialog.Builder(context!!)
                .setTitle(R.string.editpost_reason)
                .setView(view)
                .setPositiveButton(R.string.send) { _, _ ->
                    presenter.onReasonEdit(editText.text.toString())
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun showExitDialog(): Boolean {
        if (formType == EditPostForm.TYPE_EDIT_POST) {
            AlertDialog.Builder(context!!)
                    .setMessage(R.string.editpost_lose_changes)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        presenter.exit()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            return true
        }
        return false
    }

    private fun showSyncDialog() {
        AlertDialog.Builder(context!!)
                .setMessage(R.string.editpost_sync)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val selectionRange = messagePanel.selectionRange
                    presenter.exitWithSync(
                            messagePanel.message,
                            selectionRange,
                            messagePanel.attachments
                    )
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    if (!showExitDialog()) {
                        presenter.exit()
                    }
                }
                .show()
    }

    companion object {
        const val ARG_THEME_NAME = "theme_name"
        const val ARG_ATTACHMENTS = "attachments"
        const val ARG_MESSAGE = "message"
        const val ARG_FORUM_ID = "forumId"
        const val ARG_TOPIC_ID = "topicId"
        const val ARG_POST_ID = "postId"
        const val ARG_ST = "st"

        fun fillArguments(args: Bundle, postId: Int, topicId: Int, forumId: Int, st: Int, themeName: String?): Bundle {
            if (themeName != null)
                args.putString(ARG_THEME_NAME, themeName)
            args.putInt(EditPostForm.ARG_TYPE, EditPostForm.TYPE_EDIT_POST)
            args.putInt(ARG_FORUM_ID, forumId)
            args.putInt(ARG_TOPIC_ID, topicId)
            args.putInt(ARG_POST_ID, postId)
            args.putInt(ARG_ST, st)
            return args
        }

        fun fillArguments(args: Bundle, form: EditPostForm, themeName: String?): Bundle {
            if (themeName != null)
                args.putString(ARG_THEME_NAME, themeName)
            args.putInt(EditPostForm.ARG_TYPE, EditPostForm.TYPE_NEW_POST)
            args.putParcelableArrayList(ARG_ATTACHMENTS, form.attachments)
            args.putString(ARG_MESSAGE, form.message)
            args.putInt(ARG_FORUM_ID, form.forumId)
            args.putInt(ARG_TOPIC_ID, form.topicId)
            args.putInt(ARG_POST_ID, form.postId)
            args.putInt(ARG_ST, form.st)
            return args
        }
    }

}
