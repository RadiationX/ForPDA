package forpdateam.ru.forpda.presentation.history

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface HistoryView : IBaseView {
    fun showHistory(items: List<HistoryItem>)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: HistoryItem)
}
