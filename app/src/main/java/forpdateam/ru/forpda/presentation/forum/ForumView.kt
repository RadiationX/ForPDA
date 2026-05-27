package forpdateam.ru.forpda.presentation.forum

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.coretypes.ForumId

/**
 * Created by radiationx on 03.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ForumView : IBaseView {
    fun showForums(forums: List<ForumItemFlat>)

    fun scrollToForum(forumId: ForumId)

    @StateStrategyType(SkipStrategy::class)
    fun onMarkRead()

    @StateStrategyType(SkipStrategy::class)
    fun onMarkAllRead()

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)
}
