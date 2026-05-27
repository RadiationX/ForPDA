package forpdateam.ru.forpda.presentation.theme

import ru.radiationx.coretypes.PostId

/**
 * Created by radiationx on 17.03.18.
 */
interface IThemePresenter {
    fun onFirstPageClick()
    fun onPrevPageClick()
    fun onNextPageClick()
    fun onLastPageClick()
    fun onSelectPageClick()

    fun onUserMenuClick(postId: PostId)
    fun onReputationMenuClick(postId: PostId)
    fun onPostMenuClick(postId: PostId)

    fun onReportPostClick(postId: PostId)
    fun onReplyPostClick(postId: PostId)
    fun onQuotePostClick(postId: PostId, text: String)
    fun onDeletePostClick(postId: PostId)
    fun onEditPostClick(postId: PostId)
    fun onVotePostClick(postId: PostId, type: Boolean)

    fun copyText(text: String)
    fun shareText(text: String)
    fun toast(text: String)
    fun log(text: String)

    fun onPollResultsClick()
    fun onPollClick()

    fun onSpoilerCopyLinkClick(postId: PostId, spoilNumber: String)
    fun onAnchorClick(postId: PostId, name: String)

    fun onPollHeaderClick(bValue: Boolean)
    fun onHatHeaderClick(bValue: Boolean)

    fun openProfile(postId: PostId)
    fun openQms(postId: PostId)
    fun openSearchUserTopic(postId: PostId)
    fun openSearchInTopic(postId: PostId)
    fun openSearchUserMessages(postId: PostId)

    fun onChangeReputationClick(postId: PostId, type: Boolean)
    fun changeReputation(postId: PostId, type: Boolean, message: String)
    fun votePost(postId: PostId, type: Boolean)
    fun openReputationHistory(postId: PostId)

    fun quoteFromBuffer(postId: PostId)
    fun reportPost(postId: PostId, message: String)
    fun deletePost(postId: PostId)
    fun createNote(postId: PostId)
    fun copyPostLink(postId: PostId)
    fun sharePostLink(postId: PostId)
    fun copyAnchorLink(postId: PostId, name: String)
    fun copySpoilerLink(postId: PostId, spoilNumber: String)
}