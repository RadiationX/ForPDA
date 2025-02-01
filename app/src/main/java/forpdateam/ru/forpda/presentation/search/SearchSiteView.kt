package forpdateam.ru.forpda.presentation.search

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SearchSiteView : IBaseView {
    fun setStyleType(type: String)


    fun updateShowAvatarState(isShow: Boolean)
    fun updateTypeAvatarState(isCircle: Boolean)
    fun updateScrollButtonState(isEnabled: Boolean)
    fun setFontSize(size: Int)

    fun showData(searchResult: SearchResult)
    fun fillSettingsData(settings: SearchSettings, fields: Map<String, List<String>>)
    fun onStartSearch(settings: SearchSettings)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: SearchItem, settings: SearchSettings)
    fun setNewsMode()
    fun setForumMode()

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showAddInFavDialog(item: SearchItem)

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
    fun deletePostUi(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showUserMenu(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showReputationMenu(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun showPostMenu(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun reportPost(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun deletePost(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun editPost(post: SearchItem.ForumPost)

    @StateStrategyType(SkipStrategy::class)
    fun votePost(post: SearchItem.ForumPost, type: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showChangeReputation(post: SearchItem.ForumPost, type: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun openAnchorDialog(post: SearchItem.ForumPost, anchorName: String)

    @StateStrategyType(SkipStrategy::class)
    fun openSpoilerLinkDialog(post: SearchItem.ForumPost, spoilNumber: String)

    @StateStrategyType(SkipStrategy::class)
    fun toast(text: String)

    @StateStrategyType(SkipStrategy::class)
    fun log(text: String)

}