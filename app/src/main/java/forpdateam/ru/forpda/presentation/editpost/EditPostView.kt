package forpdateam.ru.forpda.presentation.editpost

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.theme.ThemePage

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface EditPostView : IBaseView {
    fun onPostSend(page: ThemePage, form: EditPostForm)
    fun showForm(form: EditPostForm)

    fun setSendRefreshing(isRefreshing: Boolean)

    fun onUploadFiles(items: List<AttachmentItem>)
    fun onDeleteFiles(items: List<AttachmentItem>)

    fun showReasonDialog(form: EditPostForm)
    fun sendMessage()
}
