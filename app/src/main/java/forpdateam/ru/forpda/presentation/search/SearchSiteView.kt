package forpdateam.ru.forpda.presentation.search

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings

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
    fun showAddInFavDialog(item: IBaseForumPost)

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