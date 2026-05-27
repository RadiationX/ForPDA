package forpdateam.ru.forpda.presentation.theme

import android.webkit.JavascriptInterface
import forpdateam.ru.forpda.ui.fragments.BaseJsInterface
import ru.radiationx.coretypes.PostId

/**
 * Created by radiationx on 17.03.18.
 */
class ThemeJsInterface(
    private val presenter: IThemePresenter
) : BaseJsInterface() {

    @JavascriptInterface
    fun firstPage() = runInUiThread(Runnable { presenter.onFirstPageClick() })

    @JavascriptInterface
    fun prevPage() = runInUiThread(Runnable { presenter.onPrevPageClick() })

    @JavascriptInterface
    fun nextPage() = runInUiThread(Runnable { presenter.onNextPageClick() })

    @JavascriptInterface
    fun lastPage() = runInUiThread(Runnable { presenter.onLastPageClick() })

    @JavascriptInterface
    fun selectPage() = runInUiThread(Runnable { presenter.onSelectPageClick() })

    @JavascriptInterface
    fun showUserMenu(postId: String) =
        runInUiThread(Runnable { presenter.onUserMenuClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun showReputationMenu(postId: String) =
        runInUiThread(Runnable { presenter.onReputationMenuClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun showPostMenu(postId: String) =
        runInUiThread(Runnable { presenter.onPostMenuClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun reportPost(postId: String) =
        runInUiThread(Runnable { presenter.onReportPostClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun reply(postId: String) =
        runInUiThread(Runnable { presenter.onReplyPostClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun quotePost(text: String, postId: String) =
        runInUiThread(Runnable { presenter.onQuotePostClick(PostId(postId.toInt()), text) })

    @JavascriptInterface
    fun deletePost(postId: String) =
        runInUiThread(Runnable { presenter.onDeletePostClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun editPost(postId: String) =
        runInUiThread(Runnable { presenter.onEditPostClick(PostId(postId.toInt())) })

    @JavascriptInterface
    fun votePost(postId: String, type: Boolean) =
        runInUiThread(Runnable { presenter.onVotePostClick(PostId(postId.toInt()), type) })

    @JavascriptInterface
    fun copySelectedText(text: String) = runInUiThread(Runnable { presenter.copyText(text) })

    @JavascriptInterface
    fun toast(text: String) = runInUiThread(Runnable { presenter.toast(text) })

    @JavascriptInterface
    fun log(text: String) = runInUiThread(Runnable { presenter.log(text) })

    @JavascriptInterface
    fun showPollResults() = runInUiThread(Runnable { presenter.onPollResultsClick() })

    @JavascriptInterface
    fun showPoll() = runInUiThread(Runnable { presenter.onPollClick() })

    @JavascriptInterface
    fun copySpoilerLink(postId: String, spoilNumber: String) =
        runInUiThread(Runnable { presenter.onSpoilerCopyLinkClick(PostId(postId.toInt()), spoilNumber) })

    @JavascriptInterface
    fun setPollOpen(bValue: String) =
        runInUiThread(Runnable { presenter.onPollHeaderClick(bValue.toBoolean()) })

    @JavascriptInterface
    fun setHatOpen(bValue: String) =
        runInUiThread(Runnable { presenter.onHatHeaderClick(bValue.toBoolean()) })

    @JavascriptInterface
    fun shareSelectedText(text: String) = runInUiThread(Runnable { presenter.shareText(text) })

    @JavascriptInterface
    fun anchorDialog(postId: String, name: String) =
        runInUiThread(Runnable { presenter.onAnchorClick(PostId(postId.toInt()), name) })

}