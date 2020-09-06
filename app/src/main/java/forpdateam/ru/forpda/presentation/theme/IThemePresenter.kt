package forpdateam.ru.forpda.presentation.theme

/**
 * Created by radiationx on 17.03.18.
 */
interface IThemePresenter {
    fun onFirstPageClick()
    fun onPrevPageClick()
    fun onNextPageClick()
    fun onLastPageClick()
    fun onSelectPageClick()

    fun onUserMenuClick(postId: Int)
    fun onReputationMenuClick(postId: Int)
    fun onPostMenuClick(postId: Int)

    fun onReportPostClick(postId: Int)
    fun onReplyPostClick(postId: Int)
    fun onQuotePostClick(postId: Int, text: String)
    fun onDeletePostClick(postId: Int)
    fun onEditPostClick(postId: Int)
    fun onVotePostClick(postId: Int, type: Boolean)

    fun setHistoryBody(index: Int, body: String)

    fun copyText(text: String)
    fun shareText(text: String)
    fun toast(text: String)
    fun log(text: String)

    fun onPollResultsClick()
    fun onPollClick()

    fun onSpoilerCopyLinkClick(postId: Int, spoilNumber: String)
    fun onAnchorClick(postId: Int, name: String)

    fun onPollHeaderClick(bValue: Boolean)
    fun onHatHeaderClick(bValue: Boolean)

    fun openProfile(postId: Int)
    fun openQms(postId: Int)
    fun openSearchUserTopic(postId: Int)
    fun openSearchInTopic(postId: Int)
    fun openSearchUserMessages(postId: Int)

    fun onChangeReputationClick(postId: Int, type: Boolean)
    fun changeReputation(postId: Int, type: Boolean, message: String)
    fun votePost(postId: Int, type: Boolean)
    fun openReputationHistory(postId: Int)

    fun quoteFromBuffer(postId: Int)
    fun reportPost(postId: Int, message: String)
    fun deletePost(postId: Int)
    fun createNote(postId: Int)
    fun copyPostLink(postId: Int)
    fun sharePostLink(postId: Int)
    fun copyAnchorLink(postId: Int, name: String)
    fun copySpoilerLink(postId: Int, spoilNumber: String)
}