package forpdateam.ru.forpda.presentation.mentions

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
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
