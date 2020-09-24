package forpdateam.ru.forpda.presentation.mentions

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface MentionsView : IBaseView {
    fun showMentions(data: MentionsData)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: MentionItem)

    @StateStrategyType(SkipStrategy::class)
    fun showAddFavoritesDialog(id: Int)

    @StateStrategyType(SkipStrategy::class)
    fun onAddToFavorite(result: Boolean)
}
