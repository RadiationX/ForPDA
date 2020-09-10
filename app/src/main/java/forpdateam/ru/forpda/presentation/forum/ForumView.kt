package forpdateam.ru.forpda.presentation.forum

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree

/**
 * Created by radiationx on 03.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ForumView : IBaseView {
    fun showForums(forumRoot: ForumItemTree)
    fun scrollToForum(id: Int)

    @StateStrategyType(SkipStrategy::class)
    fun onMarkRead()

    @StateStrategyType(SkipStrategy::class)
    fun onMarkAllRead()

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)
}
