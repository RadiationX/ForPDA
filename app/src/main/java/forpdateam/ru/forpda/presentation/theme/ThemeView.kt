package forpdateam.ru.forpda.presentation.theme

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage

/**
 * Created by radiationx on 15.03.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ThemeView : IBaseView {

    fun setStyleType(type: String)

    fun syncEditPost(data: EditPostSyncData)

    fun onEventNew(event: TabNotification)
    fun onEventRead(event: TabNotification)

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun onDeleteFromFavorite(result: Boolean)

    fun onUploadFiles(items: List<AttachmentItem>)
    fun onDeleteFiles(items: List<AttachmentItem>)

    @StateStrategyType(SkipStrategy::class)
    fun findNext(next: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun findText(text: String)

    fun updateShowAvatarState(isShow: Boolean)
    fun updateTypeAvatarState(isCircle: Boolean)
    fun updateScrollButtonState(isEnabled: Boolean)
    fun setFontSize(size: Int)
    fun scrollToAnchor(anchor: String?)
    fun updateHistoryLastHtml()

    fun onLoadData(newPage: ThemePage)
    fun updateView(page: ThemePage)

    @StateStrategyType(SkipStrategy::class)
    fun setMessageRefreshing(isRefreshing: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun onMessageSent()

    @StateStrategyType(SkipStrategy::class)
    fun showDeleteInFavDialog(page: ThemePage)

    @StateStrategyType(SkipStrategy::class)
    fun showAddInFavDialog(page: ThemePage)

    @StateStrategyType(SkipStrategy::class)
    fun showNoteCreate(title: String, url: String)


    @StateStrategyType(SkipStrategy::class)
    fun firstPage()

    @StateStrategyType(SkipStrategy::class)
    fun prevPage()

    @StateStrategyType(SkipStrategy::class)
    fun nextPage()

    @StateStrategyType(SkipStrategy::class)
    fun lastPage()

    @StateStrategyType(SkipStrategy::class)
    fun selectPage()

    @StateStrategyType(SkipStrategy::class)
    fun deletePostUi(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showUserMenu(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showReputationMenu(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showPostMenu(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun reportPost(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun insertText(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun deletePost(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun editPost(post: IBaseForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun votePost(post: IBaseForumPost, type: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showChangeReputation(post: IBaseForumPost, type: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun openAnchorDialog(post: IBaseForumPost, anchorName: String)

    @StateStrategyType(SkipStrategy::class)
    fun openSpoilerLinkDialog(post: IBaseForumPost, spoilNumber: String)

    @StateStrategyType(SkipStrategy::class)
    fun toast(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun log(text: String)
}
