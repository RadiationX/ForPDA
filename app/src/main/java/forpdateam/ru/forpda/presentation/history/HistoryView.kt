package forpdateam.ru.forpda.presentation.history

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.history.HistoryItem

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface HistoryView : IBaseView {
    fun showHistory(items: List<HistoryItem>)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: HistoryItem)
}
