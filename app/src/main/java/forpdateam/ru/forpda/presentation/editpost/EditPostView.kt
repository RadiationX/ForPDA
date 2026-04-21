package forpdateam.ru.forpda.presentation.editpost

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface EditPostView : IBaseView {
    fun onPostSend(page: ThemePage, form: EditPostForm)
    fun onNoPermission()
    fun showForm(form: EditPostForm)

    fun setSendRefreshing(isRefreshing: Boolean)

    fun onUploadFiles(items: List<AttachmentItem>)
    fun onDeleteFiles(items: List<AttachmentItem>)

    fun showReasonDialog(form: EditPostForm)
    fun sendMessage()
}
